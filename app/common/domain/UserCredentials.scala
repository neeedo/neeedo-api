package common.domain

case class UserCredentials(email: Email, password: Password)

case class EncryptedUserCredentials(email: Email, md5: PasswordHash)

object EncryptedUserCredentials {
  def apply(u: UserCredentials): EncryptedUserCredentials =
    EncryptedUserCredentials(u.email, PasswordHash(u.password))
}