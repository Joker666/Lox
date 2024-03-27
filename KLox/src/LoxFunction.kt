internal class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment
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
            return returnValue.value
        }
        return null
    }

    fun bind(instance: LoxInstance?): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment)
    }

    override fun toString(): String {
        return "<fn " + declaration.name.lexeme + ">"
    }
}
