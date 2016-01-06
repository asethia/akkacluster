package com.akka.cluster.main

import com.typesafe.config.ConfigFactory
import akka.actor.{ Props, ActorSystem }
import akka.cluster.Cluster
import akka.cluster.client._
import akka.cluster.routing._
import com.akka.cluster.JobMaster._

object MasterMain {

  def main(args: Array[String]) {

    val port = if (args.isEmpty) "3551" else args(0)
    
    val masterNum = if (args.isEmpty) "1" else args(1)

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [master]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("sampleapp", config)
    
    val masterActorName="master" + masterNum
    
    val master = system.actorOf(jobMasterProps, masterActorName)
    
    ClusterClientReceptionist(system).registerService(master)
  }

}