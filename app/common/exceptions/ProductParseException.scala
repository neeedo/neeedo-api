package common.exceptions

class ProductParseException(message: String) extends Exception {
  override def getMessage = message
}
