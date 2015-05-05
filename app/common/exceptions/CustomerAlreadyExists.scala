package common.exceptions

class CustomerAlreadyExists(message: String) extends Exception {
  override def getMessage = message
}
