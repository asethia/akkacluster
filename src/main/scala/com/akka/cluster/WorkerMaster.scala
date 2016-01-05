package com.akka.cluster

import scala.concurrent.duration._
import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._
import akka.cluster.routing._
import akka.routing.RoundRobinPool
import java.util.concurrent.TimeUnit
import com.akka.cluster.model.CommModel._

object WorkerMaster {
  def workMasterprops = Props[WorkerMaster]
  def jobWorkerProps = Props[JobWorker]
}

class WorkerMaster extends Actor
  with ActorLogging with CreateWorkerRouter {
  import WorkerMaster._
  import JobMaster._
  import context._

  val router = createWorkerRouter

  val cluster = Cluster(context.system)
  
  // subscribe to cluster changes, MemberUp
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  // re-subscribe when restart
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def supervisorStrategy: SupervisorStrategy =
    SupervisorStrategy.stoppingStrategy

  var textParts = Vector[List[String]]()
  var intermediateResult = Vector[Map[String, Int]]()
  var workGiven = 0
  var workReceived = 0
  var workers = Set[ActorRef]()

  def receive = idle

  def idle: Receive = {
    case jr @ JobRequest(jobName, text) =>
      intermediateResult = Vector[Map[String, Int]]()
      textParts = text.grouped(10).toVector
      val cancellable = context.system.scheduler.schedule(0 millis, 1 millis, router, StartWorking(self))
      context.setReceiveTimeout(60 seconds)
      become(working(jobName, sender(), cancellable))
    //keep giving work to worker (those are part of router) every 100 milli seconds
    case MemberUp(m) => {
      register(m)
    }
    //When the actor first starts up, it subscribes itself to the cluster, 
    //telling the cluster to send it CurrentClusterState and MemberUp events
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register
  }

  def working(jobName: String, master: ActorRef, cancellable: Cancellable): Receive = {
    case GiveMeWork(worker: ActorRef) =>
      if (textParts.isEmpty) {
        sender() ! WorkLoadDepleted
      } else {
        sender() ! Task(textParts.head, self)
        workGiven = workGiven + 1
        textParts = textParts.tail
        workers = workers + sender()
      }
    case tr @ TaskResult(count: Map[String, Int]) =>
      intermediateResult = intermediateResult :+ count
      workReceived = workReceived + 1
      if (textParts.isEmpty && workGiven == workReceived) {
        cancellable.cancel()
        become(finishing(jobName, master, workers))
        setReceiveTimeout(Duration.Undefined)
        self ! MergeResults
      }
    
    case ReceiveTimeout =>
      if (workers.isEmpty) {
        log.info(s"No workers responded in time. Cancelling job $jobName.")
        stop(self)
      } else setReceiveTimeout(Duration.Undefined)
    
    case Terminated(worker) =>
      log.info(s"Worker $worker got terminated. Cancelling job $jobName.")
      stop(self)
  }
  
  def finishing(jobName: String,
                master: ActorRef,
                workers: Set[ActorRef]): Receive = {
    case MergeResults =>
      val mergedMap = merge()
      workers.foreach(stop(_))
      master ! WordCount(jobName, mergedMap)
      become(idle)

    case Terminated(worker) =>
      log.info(s"Job $jobName is finishing. Worker ${worker.path.name} is stopped.")
  }

  def merge(): Map[String, Int] = {
    intermediateResult.foldLeft(Map[String, Int]()) {
      (el, acc) =>
        el.map {
          case (word, count) =>
            acc.get(word).map(accCount => (word -> (accCount + count))).getOrElse(word -> count)
        } ++ (acc -- el.keys)
    }
  }
  def register(member: Member) = {
    if (member.hasRole("master")) {
      log.info(s" *************** register worker provisioner with master {} *************** ", RootActorPath(member.address))
      context.actorSelection(RootActorPath(member.address) / "user" / "master*") ! "Register"
    }
  }
}

trait CreateWorkerRouter { this: Actor =>
  def createWorkerRouter: ActorRef = {
    context.actorOf(
      ClusterRouterPool(RoundRobinPool(10), ClusterRouterPoolSettings(
        totalInstances = 100, maxInstancesPerNode = 20,
        allowLocalRoutees = false, useRole = None)).props(WorkerMaster.jobWorkerProps),
      name = "worker-router")
  }
}
