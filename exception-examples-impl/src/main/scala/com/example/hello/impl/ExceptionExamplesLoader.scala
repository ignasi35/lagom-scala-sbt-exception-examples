package com.example.hello.impl

import com.example.hello.api.ExceptionExamplesService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.deser.{DefaultExceptionSerializer, ExceptionSerializer, RawExceptionMessage}
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, MessageProtocol, TransportException}
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.{Environment, Mode}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable

class ExceptionExamplesLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ExceptionExamplesApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ExceptionExamplesApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[ExceptionExamplesService])
}

abstract class ExceptionExamplesApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  override lazy val defaultExceptionSerializer: ExceptionSerializer = wire[NonLeakyExceptionSerializer]

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[ExceptionExamplesService](wire[ExceptionExamplesServiceImpl])
}


final class NonLeakyExceptionSerializer(environment: Environment) extends ExceptionSerializer {

  private val delegate = new DefaultExceptionSerializer(environment)
  private val isProdMode = environment.mode == Mode.Prod

  override def serialize(exception: Throwable, accept: immutable.Seq[MessageProtocol]): RawExceptionMessage = {
    exception match {
      case te: TransportException if isProdMode && te.getCause != null =>
        val safeTe = new TransportException(te.errorCode, new ExceptionMessage(te.exceptionMessage.name, te.errorCode.description))
        delegate.serialize(safeTe, accept)
      case _ => delegate.serialize(exception, accept)
    }
  }

  override def deserialize(message: RawExceptionMessage): Throwable = delegate.deserialize(message)
}