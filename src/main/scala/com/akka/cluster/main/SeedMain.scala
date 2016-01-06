package com.akka.cluster.main

import com.typesafe.config.ConfigFactory
import akka.actor.{ Props, ActorSystem }

object SeedMain {

  def main(args: Array[String]) {

    val port = if (args.isEmpty) "2551" else args(0)

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [seed]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("sampleapp", config)
    
   }

}