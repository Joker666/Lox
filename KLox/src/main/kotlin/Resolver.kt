import java.util.*

private enum class FunctionType {
    NONE,
    FUNCTION
}

class Resolver(private val interpreter: Interpreter) {
    // Each element in the stack is a Map representing a single block scope.
    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE

    internal fun resolve(statements: List<Stmt>) {
        statements.forEach { resolveStmt(it) }
    }

    private fun resolveStmt(stmt: Stmt) {
        when (stmt) {
            is Stmt.Block -> {
                beginScope()
                resolve(stmt.statements)
                endScope()
            }
            is Stmt.Var -> {
                declare(stmt.name)
                resolveExpr(stmt.initializer)
                define(stmt.name)
            }
            is Stmt.Function -> {
                // Unlike variables, we define the name eagerly,
                // before resolving the function’s body.
                // This lets a function recursively refer to itself inside its own body.
                declare(stmt.name)
                define(stmt.name)

                // In a static analysis, we immediately traverse into the body right then and there.
                resolveFunction(stmt, FunctionType.FUNCTION)
            }
            is Stmt.Expression -> resolveExpr(stmt.expression)
            is Stmt.If -> {
                // Static analysis analyzes any branch that could be run.
                // Since either one could be reached at runtime, we resolve both.
                resolveExpr(stmt.condition)
                resolveStmt(stmt.thenBranch)
                if (stmt.elseBranch != null) resolveStmt(stmt.elseBranch)
            }
            is Stmt.Print -> resolveExpr(stmt.expression)
            is Stmt.Return -> {
                if (currentFunction == FunctionType.NONE) {
                    Lox.error(stmt.keyword, "Can't return from top-level code.")
                }

                stmt.value?.let { resolveExpr(it) }
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
            is Expr.Variable -> {
                if (scopes.isEmpty()) return

                val scope = scopes.peek()
                if (scope[expr.name.lexeme] == false) {
                    Lox.error(
                        expr.name,
                        "Can't read local variable in its own initializer."
                    ) // Clever
                } else {
                    resolveLocal(expr, expr.name)
                }
            }
            is Expr.Assign -> {
                // First, we resolve the expression for the assigned value in case
                // it also contains references to other variables.
                resolveExpr(expr.value)
                resolveLocal(expr, expr.name)
            }
            is Expr.Binary -> {
                resolveExpr(expr.left)
                resolveExpr(expr.right)
            }
            is Expr.Call -> {
                resolveExpr(expr.callee)
                expr.arguments.forEach { resolveExpr(it) }
            }
            is Expr.Grouping -> resolveExpr(expr.expression)
            is Expr.Literal -> {
                // A literal expression does not mention any variables and
                // does not contain any subexpressions so there is no work to do.
            }
            is Expr.Logical -> {
                resolveExpr(expr.left)
                resolveExpr(expr.right)
            }
            is Expr.Unary -> resolveExpr(expr.right)
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
                val depth = scopes.size - 1 - i
                interpreter.resolve(expr, depth)
                return
            }
        }
    }

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type

        beginScope()
        for (param in function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()

        currentFunction = enclosingFunction
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

        // When we declare a variable in a local scope, we already know the names of every variable
        // previously declared in that same scope. If we see a collision, we report an error.
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Already a variable with this name in this scope.")
        }

        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        scope[name.lexeme] = true
    }
}
