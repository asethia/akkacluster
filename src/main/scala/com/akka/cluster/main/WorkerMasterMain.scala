package com.akka.cluster.main

import com.typesafe.config.ConfigFactory
import akka.actor.{ Props, ActorSystem }

import com.akka.cluster.WorkerMaster._

object WorkerProvisionerMain {

  def main(args: Array[String]) {

    val port = if (args.isEmpty) "4551" else args(0)

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [worker-master]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("words", config)
    
    val workerProvisioner = system.actorOf(jobWorkerProps, "worker")

  }

}