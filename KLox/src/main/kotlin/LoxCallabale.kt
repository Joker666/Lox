internal interface LoxCallable {
    val arity: Int

    fun call(interpreter: Interpreter, args: List<Any?>): Any?
}
