package common.sphere

import common.exceptions.CustomerAlreadyExists
import io.sphere.sdk.client.ErrorResponseException

trait CustomerExceptionHandler {
  def parseSphereCustomerException(ex: Exception) = {
    val cause = if (ex.getCause != null) ex.getCause else ex

    cause match {
      case e: ErrorResponseException => {
        if (!e.getErrors.isEmpty && e.getErrors.get(0).getCode == "DuplicateField")
          throw new CustomerAlreadyExists("A user with this email address already exists")
        else
          throw new Exception("Unknown error occured")
      }
      case _ => throw new Exception("Unknown error occured")
    }
  }
}
