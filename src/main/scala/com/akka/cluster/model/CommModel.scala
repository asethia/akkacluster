package com.akka.cluster.model

import akka.actor._

object CommModel {
  case class JobRequest(name: String, text: List[String])
  case class NextJob()
  case class WordCount(name: String, map: Map[String, Int])
  case class GiveMeWork(actor: ActorRef)
  case class StartWorking(actor: ActorRef)
  case class Work(jobName: String, master: ActorRef)
  case class Enlist(worker: ActorRef)
  case class Task(input: List[String], master: ActorRef)
  case class TaskResult(count: Map[String, Int])
  case object WorkLoadDepleted
  case object MergeResults
}