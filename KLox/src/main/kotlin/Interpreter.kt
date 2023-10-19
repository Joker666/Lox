import kotlin.math.floor

class Interpreter {
    fun interpret(statements: List<Stmt?>) {
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

    private fun execute(statements: List<Stmt?>) {
        statements.forEach {
            when (it) {
                is Stmt.Print -> println(stringify(evaluate(it.expression)))
                is Stmt.Expression -> evaluate(it.expression)
                else -> {}
            }
        }
    }

    private fun evaluate(expr: Expr): Any? =
        when (expr) {
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