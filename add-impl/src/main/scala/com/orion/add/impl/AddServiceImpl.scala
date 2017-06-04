package com.orion.add.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.orion.add.api
import com.orion.add.api.AddService

/**
  * Created by utsav on 4/6/17.
  */
class AddServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends AddService {
  override def add(): ServiceCall[api.AddNumber, String] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[NumberEntity](request.number.toString)

    ref.ask(AddNumber(request.number))
  }

  override def curry(): ServiceCall[api.CurryWith, Done] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[NumberEntity](request.number.toString)

    ref.ask(CurryWith(request.number))
  }

  override def additionTopic(): Topic[api.CurriedWith] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(NumberEvent.Tags, fromOffset)
        .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(numberEvent: EventStreamElement[NumberEvent]): api.CurriedWith = {
    numberEvent.event match {
      case CurriedWith(num) => api.CurriedWith(numberEvent.entityId, num)
    }
  }
}
