abstract class Expr {
    internal class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()

    internal class Grouping(val expression: Expr) : Expr()

    internal class Literal(val value: Any?) : Expr()

    internal class Unary(val operator: Token, val right: Expr) : Expr()

    internal class Variable(val name: Token) : Expr()

    internal class Assign(val name: Token, val value: Expr) : Expr()
}
