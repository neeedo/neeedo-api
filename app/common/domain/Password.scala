package common.domain

import java.security.MessageDigest

case class Password(value: String)

case class PasswordHash(private val password: String) {
  val value = new String(MessageDigest.getInstance("MD5").digest(password.getBytes))
}

object PasswordHash {
  def apply(p: Password): PasswordHash = PasswordHash(p.value)
}