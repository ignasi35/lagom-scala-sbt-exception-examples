package com.example.hello.impl

import com.example.hello.api.{ CustomException, ExceptionExamplesService }
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.BadRequest

class ExceptionExamplesServiceImpl() extends ExceptionExamplesService {

  override def hello(id: String) = ServiceCall { _ =>
    throw BadRequest(new CustomException(s"Hello $id "))
  }


}
