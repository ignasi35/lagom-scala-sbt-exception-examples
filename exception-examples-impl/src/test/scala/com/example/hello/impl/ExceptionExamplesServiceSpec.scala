package com.example.hello.impl

import com.example.hello.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }

import scala.concurrent.Await
import scala.concurrent.duration._

class ExceptionExamplesServiceSpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
  ) { ctx =>
    new ExceptionExamplesApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[ExceptionExamplesService]

  override protected def afterAll() = server.stop()

  "exception-examples service" should {

    "say hello" in {
      val actual = intercept[CustomException] {
        Await.result(client.hello("Alice").invoke(), 5.seconds)
      }
      actual.getClass.getSimpleName === ("CustomException")
      actual.customMessage === "Hello Alice"
    }

  }
}
