abstract class Stmt {
    class Expression(val expression: Expr) : Stmt()

    class Print(val expression: Expr) : Stmt()

    class Var(val name: Token, val initializer: Expr) : Stmt()

    class Block(val statements: List<Stmt>) : Stmt()

    class Break : Stmt()

    class Continue : Stmt()

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()

    class While(val condition: Expr, val body: Stmt, val increment: Expr?) : Stmt()

    object Empty : Stmt()
}
