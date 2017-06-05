package com.orion.add.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable

/**
  * Created by utsav on 4/6/17.
  */
class NumberEntity extends PersistentEntity {

  override type Command = NumberCommand[_]
  override type State = NumberState
  override type Event = NumberEvent

  override def initialState: NumberState = NumberState(0, LocalDateTime.now().toString)

  override def behavior: Behavior = {
    case NumberState(num, _) => Actions().onCommand[CurryWith, Done] {
      case (CurryWith(number), ctx, state) =>
        ctx.thenPersist(
          CurriedWith(number)
        ) { _ =>
          ctx.reply(Done)
        }
    }.onReadOnlyCommand[AddNumber, String]{
      case (AddNumber(number), ctx, state) => {
        println(s"Current state: ${state}, adding number: ${number}")
        ctx.reply(s"${num + number}")
      }
    }.onEvent {
      case (CurriedWith(number), state) => {
        println(s"Old state: ${state}, New State: ${NumberState(number, LocalDateTime.now().toString)}")
        NumberState(number, LocalDateTime.now().toString)
      }
    }
  }
}

sealed trait NumberEvent extends AggregateEvent[NumberEvent] {
  override def aggregateTag: AggregateEventTagger[NumberEvent] = NumberEvent.Tags
}

object NumberEvent {
  val Tags = AggregateEventTag[NumberEvent]
}

case class CurriedWith(number: Int) extends NumberEvent

object CurriedWith {
  implicit val format: Format[CurriedWith] = Json.format[CurriedWith]
}

case class NumberState(value: Int, timestamp: String)

object NumberState {
  implicit val format = Json.format[NumberState]
}

sealed trait NumberCommand[R] extends ReplyType[R]

case class CurryWith(number: Int) extends NumberCommand[Done]

object CurryWith {
  implicit val format: Format[CurryWith] = Json.format[CurryWith]
}

case class AddNumber(number: Int) extends NumberCommand[String]

object AddNumber {
  implicit val format: Format[AddNumber] = Json.format[AddNumber]
}

object AddSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    JsonSerializer[AddNumber],
    JsonSerializer[CurryWith],
    JsonSerializer[CurriedWith],
    JsonSerializer[NumberState]
  )
}