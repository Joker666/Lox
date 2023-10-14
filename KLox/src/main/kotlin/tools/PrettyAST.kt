package tools

import Expr
import Token
import TokenType.MINUS
import TokenType.STAR

fun main(args: Array<String>) {
    val expression =
        Expr.Binary(
            Expr.Unary(Token(MINUS, "-", null, 1), Expr.Literal(123)),
            Token(STAR, "*", null, 1),
            Expr.Grouping(Expr.Literal(45.67))
        )

    PrettyAST.print(expression)
}

object PrettyAST {
    fun print(expr: Expr) {
        println(prettify(expr))
    }

    // prettify an expression
    private fun prettify(expr: Expr): String =
        when (expr) {
            is Expr.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
            is Expr.Grouping -> parenthesize("group", expr.expression)
            is Expr.Literal -> if (expr.value == null) "nil" else expr.value.toString()
            is Expr.Unary -> parenthesize(expr.operator.lexeme, expr.right)
            is Expr.Variable -> parenthesize(expr.name.lexeme)
            else -> ""
        }

    private fun parenthesize(name: String, vararg expressions: Expr): String {
        val expressionStrings = expressions.map { expr -> prettify(expr) }
        val concatenatedExpressions = expressionStrings.reduce { acc, expr -> "$acc $expr" }
        return "($name $concatenatedExpressions)"
    }
}
