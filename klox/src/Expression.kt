abstract class Expr {
    internal class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()

    internal class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr()

    internal class Grouping(val expression: Expr) : Expr()

    internal class Literal(val value: Any?) : Expr()

    internal class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()

    internal class Unary(val operator: Token, val right: Expr) : Expr()

    class Variable(val name: Token) : Expr()

    internal class Assign(val name: Token, val value: Expr) : Expr()

    internal class Get(val loxObject: Expr, val name: Token) : Expr()

    internal class Set(val loxObject: Expr, val name: Token, val value: Expr) : Expr()

    internal class Super(val keyword: Token, val method: Token) : Expr()

    internal class This(val keyword: Token) : Expr()

    object Empty : Expr()
}
