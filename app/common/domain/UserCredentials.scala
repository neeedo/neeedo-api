package common.domain

case class UserCredentials(email: Email, password: Password) {
  val cacheKey =  s"userCredentials.${email.value}"
}

case class EncryptedUserCredentials(id: UserId, email: Email, md5: PasswordHash)

object EncryptedUserCredentials {
  def apply(userId: UserId, u: UserCredentials): EncryptedUserCredentials =
    EncryptedUserCredentials(userId, u.email, PasswordHash(u.password))
}