package common.exceptions

class CustomerAlreadyExists(message: String) extends Exception {
  override def getMessage = message
}

class ProductNotFound(message: String) extends Exception {
  override def getMessage = message
}

class ElasticSearchIndexFailed(message: String) extends Exception {
  override def getMessage = message
}

class ElasticSearchQueryFailed(message: String) extends Exception {
  override def getMessage = message
}

class SphereIndexFailed(message: String) extends Exception {
  override def getMessage = message
}
