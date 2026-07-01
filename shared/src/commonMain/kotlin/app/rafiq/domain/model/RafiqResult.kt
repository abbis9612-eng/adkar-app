package app.rafiq.domain.model

sealed class RafiqResult<out T> {
    data class Success<T>(val data: T)                                  : RafiqResult<T>()
    data class Error(val message: String, val type: ErrorType = ErrorType.GENERIC) : RafiqResult<Nothing>()
    data object Loading                                                  : RafiqResult<Nothing>()
}

enum class ErrorType { GENERIC, NETWORK, LOCATION, DATABASE, PERMISSION }
