import java.util.*

class Resolver(private val interpreter: Interpreter) {
    // Each element in the stack is a Map representing a single block scope.
    private val scopes = Stack<MutableMap<String, Boolean>>()

    private fun resolve(statements: List<Stmt>) {
        statements.forEach { resolveStmt(it) }
    }

    private fun resolveStmt(stmt: Stmt) {
        when (stmt) {
            is Stmt.Block -> {
                beginScope()
                resolve(stmt.statements)
                endScope()
            }
            is Stmt.Expression -> resolveExpr(stmt.expression)
            is Stmt.Function -> {
                declare(stmt.name)
                define(stmt.name)
                resolveFunction(stmt)
            }
            is Stmt.If -> {
                resolveExpr(stmt.condition)
                resolveStmt(stmt.thenBranch)
                if (stmt.elseBranch != null) resolveStmt(stmt.elseBranch)
            }
            is Stmt.Print -> resolveExpr(stmt.expression)
            is Stmt.Return -> stmt.value?.let { resolveExpr(it) }
            is Stmt.Var -> {
                declare(stmt.name)
                resolveExpr(stmt.initializer)
                define(stmt.name)
            }
            is Stmt.While -> {
                resolveExpr(stmt.condition)
                resolveStmt(stmt.body)
            }
            else -> {} // do nothing, we don't care about other statements yet.
        }
    }

    private fun resolveExpr(expr: Expr) {
        when (expr) {
            is Expr.Assign -> {
                resolveExpr(expr.value)
                resolveLocal(expr, expr.name)
            }
            is Expr.Binary -> resolveExpr(expr.left)
            is Expr.Call -> {
                resolveExpr(expr.callee)
                expr.arguments.forEach { resolveExpr(it) }
            }
            is Expr.Grouping -> resolveExpr(expr.expression)
            is Expr.Literal -> {}
            is Expr.Logical -> resolveExpr(expr.left)
            is Expr.Unary -> resolveExpr(expr.right)
            is Expr.Variable -> {
                if (scopes.isEmpty()) return

                val scope = scopes.peek()
                if (scope[expr.name.lexeme] == false) {
                    Lox.error(expr.name, "Can't read local variable in its own initializer.")
                } else {
                    resolveLocal(expr, expr.name)
                }
            }
            else -> {} // do nothing, we don't care about other expressions yet.
        }
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        // We start at the innermost scope and work outwards,
        // looking in each map for a matching name.
        for (i in scopes.indices.reversed()) {
            if (scopes[i].containsKey(name.lexeme)) {
                // If we find the variable, we resolve it, passing in the number of scopes between
                // the current innermost scope and the scope where the variable was found. So, if
                // the variable was found in the current scope, we pass in 0. If it’s in the
                // immediately enclosing scope, 1.
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun resolveFunction(function: Stmt.Function) {
        beginScope()
        for (param in function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()
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
