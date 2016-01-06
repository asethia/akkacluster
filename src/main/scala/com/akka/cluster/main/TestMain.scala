package com.akka.cluster.main

import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.cluster.client._
import scala.concurrent.duration.FiniteDuration
import java.time.Duration
import java.util.concurrent.TimeUnit
import com.akka.cluster.model.CommModel._

object TestMain {

  def main(args: Array[String]) {

    val config = ConfigFactory.parseString("""
     akka {
       actor {
         provider = "akka.remote.RemoteActorRefProvider"
       }

       remote {
         transport = "akka.remote.netty.NettyRemoteTransport"
         log-remote-lifecycle-events = off
         netty.tcp {
          hostname = "127.0.0.1"
          port = 5000
         }
       }
     }""")
    val system = ActorSystem("Test", ConfigFactory.load(config))

    val initialContacts = Set(ActorPath.fromString("akka.tcp://sampleapp@127.0.0.1:3551/system/receptionist"))

    val settings = ClusterClientSettings(system)
      .withInitialContacts(initialContacts)

    val c = system.actorOf(ClusterClient.props(settings))

    val text = List("this is a test", "of some very naive word counting", "but what can you say", "it is what it is")

    c ! ClusterClient.Send("/user/master1", JobRequest("test1", (1 to 5).flatMap(i => text ++ text).toList), localAffinity = true)
    c ! ClusterClient.Send("/user/master1", JobRequest("test2", (1 to 2).flatMap(i => text ++ text).toList), localAffinity = true)
    c ! ClusterClient.Send("/user/master1", JobRequest("test3", (1 to 7).flatMap(i => text ++ text).toList), localAffinity = true)

  }

}