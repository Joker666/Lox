abstract class Stmt {
    class Expression(val expression: Expr) : Stmt()

    class Print(val expression: Expr) : Stmt()

    class Var(val name: Token, val initializer: Expr) : Stmt()
}
