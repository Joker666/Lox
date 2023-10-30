abstract class Stmt {
    class Expression(val expression: Expr) : Stmt()

    class Print(val expression: Expr) : Stmt()

    class Var(val name: Token, val initializer: Expr) : Stmt()

    class Block(val statements: List<Stmt>) : Stmt()

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()

    class While(val condition: Expr, val body: Stmt) : Stmt()

    object Empty : Stmt()
}
