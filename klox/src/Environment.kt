class Environment(private val enclosing: Environment?) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        return when {
            contains(name.lexeme) -> values[name.lexeme]
            enclosing != null -> enclosing.get(name) // clever
            else -> throw RuntimeError(name, "Undefined variable '" + name.lexeme + "'.")
        }
    }

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values[name]
    }

    fun assign(name: Token, value: Any?): Unit =
        when {
            contains(name.lexeme) -> define(name.lexeme, value)
            enclosing != null -> enclosing.assign(name, value) // clever
            else -> throw RuntimeError(name, "Undefined variable '" + name.lexeme + "'.")
        }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    // This walks a fixed number of hops up the parent chain and returns the environment there
    private fun ancestor(distance: Int): Environment {
        var environment: Environment = this
        for (i in 0 ..< distance) {
            environment = environment.enclosing!!
        }

        return environment
    }

    private fun contains(name: String): Boolean = values.keys.contains(name)
}
