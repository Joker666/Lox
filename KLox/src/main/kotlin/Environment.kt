class Environment(private val enclosing: Environment?) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        return when {
            contains(name.lexeme) -> values[name.lexeme]
            enclosing != null -> enclosing.get(name)
            else -> throw RuntimeError(name, "Undefined variable '" + name.lexeme + "'.")
        }
    }

    fun assign(name: Token, value: Any?): Unit =
        when {
            contains(name.lexeme) -> define(name.lexeme, value)
            enclosing != null -> enclosing.assign(name, value)
            else -> throw RuntimeError(name, "Undefined variable '" + name.lexeme + "'.")
        }

    private fun contains(name: String): Boolean = values.keys.contains(name)
}
