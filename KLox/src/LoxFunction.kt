internal class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
    private val isInitializer: Boolean,
) : LoxCallable {
    override fun arity(): Int {
        return declaration.params.size
    }

    // call handled function execution. It is one of the most clever solutions that makes sure
    // the function encapsulates its parameters. This means each function gets its own environment.
    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        val environment = Environment(closure)

        // bind the arguments to the parameters
        for (i in declaration.params.indices) {
            environment.define(declaration.params[i].lexeme, args[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: ReturnException) {
            // If we’re in an initializer and execute a return statement, instead of returning the
            // value (which will always be nil), we again return this.
            if (isInitializer) {
                return closure.getAt(0, "this")
            }
            return returnValue.value
        }

        // If the function is an initializer,
        // we override the actual return value and forcibly return this.
        if (isInitializer) {
            return closure.getAt(0, "this")
        }
        return null
    }

    fun bind(instance: LoxInstance?): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }

    override fun toString(): String {
        return "<fn " + declaration.name.lexeme + ">"
    }
}
