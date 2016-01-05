package com.akka.cluster

import scala.concurrent.duration._

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._
import com.akka.cluster.model.CommModel._

class JobWorker extends Actor
  with ActorLogging {

  import context._
  var processed = 0

  def receive=idle
  
  def idle: Receive = {
    case StartWorking(master) =>
      become(working)
      master ! GiveMeWork(self)
    case Terminated(master) =>
      setReceiveTimeout(Duration.Undefined)
      log.error(s"Master terminated that ran Job , stopping self.")
      stop(self)
  }

  def working: Receive = {
    case Task(textPart, master) =>
      val countMap = processTask(textPart)
      processed = processed + 1
      master ! TaskResult(countMap)
      become(idle)
      setReceiveTimeout(30 seconds)
    case WorkLoadDepleted =>
      context.become(idle)
      setReceiveTimeout(30 seconds)
  }

  def processTask(textPart: List[String]): Map[String, Int] = {
    textPart.flatMap(_.split("\\W+"))
      .foldLeft(Map.empty[String, Int]) {
        (count, word) =>
          if (word == "FAIL") throw new RuntimeException("SIMULATED FAILURE!")
          count + (word -> (count.getOrElse(word, 0) + 1))
      }
  }
}
