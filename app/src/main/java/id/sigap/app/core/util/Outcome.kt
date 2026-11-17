package id.sigap.app.core.util

/** Result of an operation that can fail, without using null as an error. */
sealed interface Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>
    data class Failure(val cause: Throwable) : Outcome<Nothing>
}
