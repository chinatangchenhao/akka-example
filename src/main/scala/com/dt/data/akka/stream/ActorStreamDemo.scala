package com.dt.data.akka.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object ActorStreamDemo {
  def main(args: Array[String]): Unit = {
    implicit  val system = ActorSystem.create("sys")
    implicit val materializer = ActorMaterializer()
    val source = Source(1 to 5)
    val sink = Sink.foreach(println)
    val runnableGraph = source.to(sink)
    runnableGraph.run()
  }
}
