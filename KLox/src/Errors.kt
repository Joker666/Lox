class RuntimeError(val token: Token, message: String?) : RuntimeException(message)

/**
 * Exit codes, c/o BSD
 * https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
 */
enum class ExitCodes(val value: Int) {
    /**
     * The command was used incorrectly, e.g., with the wrong number of arguments, a bad flag, a bad
     * syntax in a parameter, or whatever.
     */
    USAGE(64),

    /**
     * The input data was incorrect in some way. This should only be used for user's data and not
     * system files.
     */
    DATA_ERR(65),

    /**
     * An internal software error has been detected. This should be limited to non-operating system
     * related errors as possible.
     */
    SOFTWARE(70)
}

class BreakException : RuntimeException()

class ContinueException : RuntimeException()

class ReturnException(val value: Any?) : RuntimeException()
