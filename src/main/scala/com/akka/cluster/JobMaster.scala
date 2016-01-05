package com.akka.cluster

import scala.concurrent.duration._
import akka.actor._
import akka.cluster._
import akka.routing._
import akka.cluster.ClusterEvent._
import akka.cluster.routing._
import com.akka.cluster.model.CommModel._

/**
 * This is Job Master, accepts new job and assign to associated worker master
 */
object JobMaster {
  def jobMasterProps = Props(new JobMaster)
}

class JobMaster extends Actor
  with ActorLogging {

  import JobMaster._

  var workers: List[ActorRef] = List()
  var jobList: List[JobRequest] = List()

  override def supervisorStrategy: SupervisorStrategy =
    SupervisorStrategy.stoppingStrategy

  def receive = startWorking

  def startWorking: Receive = {
    case "Register" if !workers.contains(sender()) =>
      log.info(s" *************** worker provisioner registered with master path {} *************** ", self.path)
      context watch sender()
      workers = workers :+ sender()
    case jr @ JobRequest(name, text) =>
      workers match {
        //if WorkerMaster is the last one, assign this job to him
        case nextWorkerProvisioner :: Nil => {
          workers = Nil
          nextWorkerProvisioner ! jr
        }
        //if WorkerMaster is available assign this job to him
        case nextWorkerProvisioner :: tailWorkerProvisioner => {
          workers = tailWorkerProvisioner
          nextWorkerProvisioner ! jr
        }
        //if no more WorkerMaster available put this job into queue
        case Nil => jobList = jobList :+ jr 
      }
    case NextJob => {
      jobList match {
        //if Job queue has last job start working 
        case nextJob :: Nil     => { self ! nextJob }
        //if job queue has more than one job, start working on the first one 
        case nextJob :: tailJob => { jobList = tailJob; self ! nextJob }
        case Nil                => log.info("No more jobs in the queue")
      }
    }
    case Terminated(a) =>
      workers = workers.filterNot(_ == a)
    case WordCount(jobName, map) => {
      log.info(s" job ${jobName} result is :${map}")
      workers = workers :+ sender()
      self ! NextJob
    }
  }
}



