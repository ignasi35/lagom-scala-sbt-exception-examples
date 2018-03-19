package com.example.hello.impl

import akka.NotUsed
import com.example.hello.api.{CustomException, ExceptionExamplesService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.BadRequest

import scala.concurrent.Future

class ExceptionExamplesServiceImpl() extends ExceptionExamplesService {

  override def hello(id: String) = ServiceCall { _ =>
    throw BadRequest(new CustomException(s"Hello $id "))
  }

  /**
    * TO TEST:
    * --------
    *
    * - in a console, do:             `sbt clean docker:publishLocal`
    * - launch the generated script:  `./exception-examples-impl/target/docker/stage/opt/docker/bin/exception-examples-impl`
    * - in another console:           `curl http://localhost:9000/api/disclose`
    */
  override def disclose: ServiceCall[NotUsed, String] = ServiceCall { _ =>
    val cause = new RuntimeException(s"This message should not be disclosed to the public")

    Future.failed(BadRequest(cause))
  }
}
