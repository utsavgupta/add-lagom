package com.orion.add.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.orion.add.api.AddService
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

/**
  * Created by utsav on 4/6/17.
  */
class AddServiceLoader extends LagomApplicationLoader{
  override def load(context: LagomApplicationContext): LagomApplication =
    new AddApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AddApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[AddService]
  )
}

abstract class AddApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with CassandraPersistenceComponents
  with LagomKafkaComponents
  with AhcWSComponents {

  override lazy val lagomServer = serverFor[AddService](wire[AddServiceImpl])
  override lazy val jsonSerializerRegistry = AddSerializerRegistry

  persistentEntityRegistry.register(wire[NumberEntity])
}
