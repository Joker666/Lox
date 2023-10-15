class Interpreter {
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
                    TokenType.MINUS -> left as Double - right as Double
                    TokenType.PLUS -> {
                        if (left is Double && right is Double) left + right // Handle numbers
                        else if (left is String && right is String)
                            left + right // Handle string concatenation
                        else null
                    }
                    TokenType.STAR -> left as Double * right as Double
                    TokenType.SLASH -> left as Double / right as Double
                    TokenType.GREATER -> left as Double > right as Double
                    TokenType.GREATER_EQUAL -> left as Double >= right as Double
                    TokenType.LESS -> (left as Double) < right as Double
                    TokenType.LESS_EQUAL -> left as Double <= right as Double
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
}
