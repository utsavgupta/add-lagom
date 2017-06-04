package com.orion.add.api

import akka.Done
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}


/**
  * Created by utsav on 4/6/17.
  */

object AddService {
  val TOPIC_NAME = "addition"
}

trait AddService extends Service {

  def add(): ServiceCall[AddNumber, String]

  def curry(): ServiceCall[CurryWith, Done]

  def additionTopic(): Topic[CurriedWith]

  override final def descriptor: Descriptor = {
    import Service._

    named("add")
      .withCalls(
        pathCall("/api/add/curry", curry _),
        pathCall("/api/add/result", add _)
      )
      .withTopics(
        topic(AddService.TOPIC_NAME, additionTopic _)
          .addProperty(KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[CurriedWith](_.id))
      )
      .withAutoAcl(true)
  }
}

case class AddNumber(number: Int)

object AddNumber {
  implicit val format: Format[AddNumber] = Json.format[AddNumber]
}

case class CurryWith(number: Int)

object CurryWith {
  implicit val format: Format[CurryWith] = Json.format[CurryWith]
}

case class CurriedWith(id: String, number: Int)

object CurriedWith {
  implicit val format: Format[CurriedWith] = Json.format[CurriedWith]
}