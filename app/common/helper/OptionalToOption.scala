package common.helper

object OptionalToOptionConverter {
  implicit def optionalToOption[T](opt: java.util.Optional[T]): Option[T] = {
    if (opt.isPresent) Some[T](opt.get())
    else Option.empty[T]
  }
}
