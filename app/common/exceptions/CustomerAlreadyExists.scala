package common.exceptions

class CustomerAlreadyExists(message: String) extends Exception(message)
class ProductNotFound(message: String) extends Exception(message)
class ElasticSearchIndexFailed(message: String) extends Exception(message)
class ElasticSearchQueryFailed(message: String) extends Exception(message)
class SphereIndexFailed(message: String) extends Exception(message)
class InvalidConfiguration(message: String) extends Exception(message)

