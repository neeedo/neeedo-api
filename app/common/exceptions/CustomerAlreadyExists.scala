package common.exceptions

class CustomerAlreadyExists(message: String) extends Exception(message)
class UserNotFound(message: String) extends Exception(message)
class ProductNotFound(message: String) extends Exception(message)
class ElasticSearchIndexFailed(message: String) extends Exception(message)
class ElasticSearchDeleteFailed(message: String) extends Exception(message)
class ElasticSearchQueryFailed(message: String) extends Exception(message)
class SphereIndexFailed(message: String) extends Exception(message)
class SphereDeleteFailed(message: String) extends Exception(message)
class InvalidConfiguration(message: String) extends Exception(message)
class InvalidJson(message: String) extends Exception(message)
class Unauthorized(message: String) extends Exception(message)
class MalformedOffer(message: String) extends Exception(message)
class MalformedDemand(message: String) extends Exception(message)
class NetworkProblem(message: String) extends Exception(message)
class UploadFileToLarge(message: String) extends Exception(message)
class WrongUploadType(message: String) extends Exception(message)

