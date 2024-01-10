import java.util.*

class Resolver(private val interpreter: Interpreter) {
    private val scopes = Stack<MutableMap<String, Boolean>>()

    private fun resolve(statements: List<Stmt>) {
        statements.forEach { resolve(it) }
    }

    private fun resolve(stmt: Stmt) {
        when (stmt) {
            is Stmt.Block -> {
                beginScope()
                resolve(stmt.statements)
                endScope()
            }
            is Stmt.Expression -> resolve(stmt.expression)
            is Stmt.Function -> {
                declare(stmt.name)
                define(stmt.name)
                resolve(stmt.body)
            }
            is Stmt.If -> {
                resolve(stmt.condition)
                resolve(stmt.thenBranch)
                if (stmt.elseBranch != null) resolve(stmt.elseBranch)
            }
            is Stmt.Print -> resolve(stmt.expression)
            is Stmt.Return -> stmt.value?.let { resolve(it) }
            is Stmt.Var -> {
                declare(stmt.name)
                resolve(stmt.initializer)
                define(stmt.name)
            }
            is Stmt.While -> {
                resolve(stmt.condition)
                resolve(stmt.body)
            }
            else -> {} // do nothing, we don't care about other statements yet.
        }
    }

    private fun resolve(expr: Expr) {
        when (expr) {
            is Expr.Assign -> resolve(expr.value)
            is Expr.Binary -> resolve(expr.left)
            is Expr.Call -> {
                resolve(expr.callee)
                expr.arguments.forEach { resolve(it) }
            }
            is Expr.Grouping -> resolve(expr.expression)
            is Expr.Literal -> {}
            is Expr.Logical -> resolve(expr.left)
            is Expr.Unary -> resolve(expr.right)
            is Expr.Variable -> {
                if (scopes.isEmpty()) return

                val scope = scopes.peek()
                if (scope[expr.name.lexeme] == false) {
                    Lox.error(expr.name, "Can't read local variable in its own initializer.")
                }
            }
            else -> {} // do nothing, we don't care about other expressions yet.
        }
    }

    private fun beginScope() {
        scopes.push(HashMap())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        scope[name.lexeme] = true
    }
}
