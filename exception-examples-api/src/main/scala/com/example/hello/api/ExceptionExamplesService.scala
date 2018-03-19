package com.example.hello.api

import akka.NotUsed
import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.{DefaultExceptionSerializer, ExceptionSerializer, RawExceptionMessage}
import com.lightbend.lagom.scaladsl.api.transport.{MessageProtocol, TransportException}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.{Environment, Mode}

import scala.collection.immutable.Seq
import scala.util.control.NoStackTrace

trait ExceptionExamplesService extends Service {


  def hello(id: String): ServiceCall[NotUsed, String]

  def disclose: ServiceCall[NotUsed, String]

  override final def descriptor = {
    import Service._
    named("exception-examples")
      .withCalls(
        pathCall("/api/hello/:id", hello _),
        pathCall("/api/disclose", disclose _)
      )
      //.withExceptionSerializer(new CustomExceptionSerializer())
      .withAutoAcl(true)
  }
}

class CustomException(val customMessage: String) extends Exception(customMessage) with NoStackTrace

class CustomExceptionSerializer() extends ExceptionSerializer {
  private val delegate = new DefaultExceptionSerializer(Environment.simple(mode = Mode.Prod))

  private val MARK = ByteString('#')
  private val METADATA_MARK = ByteString('@')

  override def serialize(exception: Throwable, accept: Seq[MessageProtocol]): RawExceptionMessage = {
    val rawMessage = delegate.serialize(exception, accept)

    // MARK should be escaped, this is not a production ready Serializer)
    if (exception.isInstanceOf[TransportException] && exception.getCause != null) {
      val causeName = ByteString(s"${exception.getCause.getClass.getName}")
      val causeMessage = ByteString(s"${exception.getCause.getMessage}")
      val metadata = causeName ++ MARK ++ causeMessage
      val messageWithMetadata = metadata ++ METADATA_MARK ++ rawMessage.message
      // CustomException#the-message@delegateRawMessage
      RawExceptionMessage(rawMessage.errorCode, rawMessage.protocol, messageWithMetadata)
    } else {
      rawMessage
    }

  }

  override def deserialize(rawMessage: RawExceptionMessage): Throwable = {
    val throwable = delegate.deserialize(rawMessage)
    val (b1, b2) = rawMessage.message.span(_ != METADATA_MARK.head)
    (b1, b2) match {
      case (_, ByteString.empty) => throwable
      case (head, _) =>
        head.span(_ != MARK.head) match {
          case (name, msg) if name == ByteString(classOf[CustomException].getName) => new CustomException(msg.tail.utf8String)
          case _ => throwable
        }
    }
  }
}
