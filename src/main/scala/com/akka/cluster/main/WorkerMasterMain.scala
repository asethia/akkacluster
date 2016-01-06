package com.akka.cluster.main

import com.typesafe.config.ConfigFactory
import akka.actor.{ Props, ActorSystem }

import com.akka.cluster.WorkerMaster._

object WorkerMasterMain {

  def main(args: Array[String]) {

    val port = if (args.isEmpty) "4551" else args(0)

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [workermaster]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("sampleapp", config)
    
    val workerProvisioner = system.actorOf(workMasterprops, "workermaster")

  }

}