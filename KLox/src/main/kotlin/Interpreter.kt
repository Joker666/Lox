import kotlin.math.floor

class Interpreter {
    // globals field holds a fixed reference to the outermost global environment.
    private val globals: Environment = Environment(null)
    private var environment = globals

    init {
        globals.define(
            "clock",
            object : LoxCallable {
                override fun arity(): Int = 0

                override fun call(interpreter: Interpreter, args: List<Any?>): Double {
                    return System.currentTimeMillis() / 1000.0
                }

                override fun toString(): String {
                    return "<native fn>"
                }
            }
        )
    }

    fun interpret(statements: List<Stmt>) {
        try {
            execute(statements)
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    private fun stringify(obj: Any?): String =
        when (obj) {
            null -> "nil"
            is Double ->
                if (floor(obj) == obj) { // convert 5.0 => 5
                    obj.toInt().toString()
                } else obj.toString()
            else -> obj.toString()
        }

    private fun execute(statements: List<Stmt>) {
        statements.forEach {
            when (it) {
                is Stmt.Print -> println(stringify(evaluate(it.expression)))
                is Stmt.Expression -> evaluate(it.expression)
                is Stmt.Function -> environment.define(it.name.lexeme, LoxFunction(it, environment))
                is Stmt.Return -> executeReturn(it)
                is Stmt.Var -> environment.define(it.name.lexeme, evaluate(it.initializer))
                is Stmt.If -> executeIf(it)
                is Stmt.Break -> throw BreakException()
                is Stmt.Continue -> throw ContinueException()
                is Stmt.While -> executeWhile(it)
                is Stmt.Block -> executeBlock(it.statements, Environment(enclosing = environment))
                else -> {}
            }
        }
    }

    // executeReturn executes a return statement.
    // It evaluates the return value and throws a ReturnException with the evaluated value.
    private fun executeReturn(stmt: Stmt.Return) {
        val value = stmt.value?.let { evaluate(it) }
        throw ReturnException(value)
    }

    // executeIf executes an if statement.
    // It evaluates the condition and executes the thenBranch if the condition is truthy.
    // If the elseBranch is present, it is executed if the condition is falsy.
    // If the elseBranch is not present, it returns an empty statement.
    private fun executeIf(statement: Stmt.If) {
        val statements =
            listOf(
                if (evaluate(statement.condition).isTruthy()) {
                    statement.thenBranch
                } else statement.elseBranch ?: Stmt.Empty
            )
        execute(statements) // recursion
    }

    // executeWhile executes a while statement.
    // It evaluates the condition and executes the body if the condition is truthy.
    // It repeats the process until the condition is falsy.
    // It handles break and continue statements with clever solution.
    private fun executeWhile(statement: Stmt.While) {
        try {
            while (evaluate(statement.condition).isTruthy()) {
                try {
                    execute(listOf(statement.body))
                } catch (e: ContinueException) {
                    // do nothing
                } finally {
                    statement.increment?.let { evaluate(it) }
                }
            }
        } catch (e: BreakException) {
            // do nothing
        }
    }

    // executeBlock executes a list of statements within a new environment.
    // It executes the statements by recursively calling the execute function.
    // The previous environment is restored after the statements are executed.
    // This maintains the lexical scoping of variables. Clever solution.
    fun executeBlock(statements: List<Stmt>, environment: Environment?) {
        val previous = this.environment
        try {
            this.environment = environment!!
            execute(statements) // recursion
        } finally {
            this.environment = previous
        }
    }

    private fun evaluate(expr: Expr): Any? {
        return when (expr) {
            is Expr.Literal -> expr.value
            is Expr.Grouping -> evaluate(expr.expression)
            is Expr.Unary -> {
                val right = evaluate(expr.right)
                when (expr.operator.type) {
                    TokenType.MINUS -> -(right as Double)
                    TokenType.BANG -> !right.isTruthy()
                    else -> null
                }
            }
            is Expr.Call -> {
                val callee = evaluate(expr.callee)
                val arguments = expr.arguments.map { evaluate(it) }
                if (callee is LoxCallable) {
                    if (arguments.size != callee.arity()) {
                        throw RuntimeError(
                            expr.paren,
                            "Expected ${callee.arity()} arguments but got ${arguments.size}."
                        )
                    }
                    callee.call(this, arguments)
                } else {
                    throw RuntimeError(expr.paren, "Can only call functions and classes.")
                }
            }
            is Expr.Variable -> environment.get(expr.name)
            is Expr.Assign -> {
                val value = evaluate(expr.value)
                environment.assign(expr.name, value)
                value
            }
            is Expr.Logical -> {
                val left = evaluate(expr.left)
                if (expr.operator.type == TokenType.OR) {
                    if (left.isTruthy()) return left
                } else { // AND operator
                    if (!left.isTruthy()) return left
                }
                return evaluate(expr.right) // recursion
            }
            is Expr.Binary -> {
                val left = evaluate(expr.left)
                val right = evaluate(expr.right)
                when (expr.operator.type) {
                    TokenType.MINUS -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double - right as Double
                    }
                    TokenType.PLUS -> {
                        if (left is Double && right is Double) left + right // Handle numbers
                        else if (left is String && right is String)
                            left + right // Handle string concatenation
                        else
                            throw RuntimeError(
                                expr.operator,
                                "Operands must be numbers or strings."
                            )
                    }
                    TokenType.STAR -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double * right as Double
                    }
                    TokenType.SLASH -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double / right as Double
                    }
                    TokenType.GREATER -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double > right as Double
                    }
                    TokenType.GREATER_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double >= right as Double
                    }
                    TokenType.LESS -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) < right as Double
                    }
                    TokenType.LESS_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double <= right as Double
                    }
                    TokenType.BANG_EQUAL -> !isEqual(left, right)
                    TokenType.EQUAL_EQUAL -> isEqual(left, right)
                    else -> null
                }
            }
            else -> null
        }
    }

    // Lox follows Ruby’s simple rule: false and nil are falsey, and everything else is truthy.
    private fun Any?.isTruthy(): Boolean =
        when (this) {
            null -> false
            is Boolean -> this
            else -> true
        }

    private fun isEqual(a: Any?, b: Any?): Boolean =
        when {
            a == null && b == null -> true
            a == null -> false
            else -> a == b
        }

    // checkNumberOperands is a helper function that checks if all operands are numbers.
    // If any operand is not a number, it throws a RuntimeError.
    // This is useful because it allows us to catch errors early in the program.
    private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
        operands
            .filterNot { it is Double }
            .ifEmpty {
                return
            }
        throw RuntimeError(operator, "Operand must be a number.")
    }
}
