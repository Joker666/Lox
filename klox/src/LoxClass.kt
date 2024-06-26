internal class LoxClass(
    val name: String,
    private val superclass: LoxClass?,
    private val methods: MutableMap<String, LoxFunction>
) : LoxCallable {

    // We look for a method on the current class before walking up the superclass chain.
    fun findMethod(name: String): LoxFunction? {
        return methods[name] ?: superclass?.findMethod(name)
    }

    override fun arity(): Int {
        val initializer = findMethod("init")
        return initializer?.arity() ?: 0
    }

    override fun call(interpreter: Interpreter, args: List<Any?>): Any {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, args)
        return instance
    }

    override fun toString(): String {
        return name
    }
}

internal class LoxInstance(private val klass: LoxClass) {
    private val fields = mutableMapOf<String, Any?>()

    fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }

        val method = klass.findMethod(name.lexeme)
        if (method != null) {
            return method.bind(this)
        }

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return klass.name + " instance"
    }
}
