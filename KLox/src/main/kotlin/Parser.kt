import TokenType.*

enum class FunctionKind {
    FUNCTION,
    METHOD,
    INITIALIZER,
}

internal class Parser(private val tokens: List<Token>) {
    private class ParseError : RuntimeException()

    private var index = 0
    private var loopDepth = 0

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    // declaration   → classDecl
    //               | funDecl
    //               | varDecl
    //               | statement ;
    private fun declaration(): Stmt {
        return try {
            if (match(CLASS)) return classDeclaration()
            if (match(FUN)) return funDeclaration(FunctionKind.FUNCTION)
            if (match(VAR)) return varDeclaration()

            return statement()
        } catch (error: ParseError) {
            synchronize()
            Stmt.Empty
        }
    }

    // classDecl     → "class" IDENTIFIER "{" function* "}" ;
    private fun classDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect class name.")
        consume(LEFT_BRACE, "Expect '{' before class body.")

        val methods = mutableListOf<Stmt.Function>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(funDeclaration(FunctionKind.METHOD) as Stmt.Function)
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.")
        return Stmt.Class(name, methods)
    }

    // funDecl       → "fun" function ;
    private fun funDeclaration(kind: FunctionKind): Stmt {
        val name = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expect '(' after function name.")
        val parameters = mutableListOf<Token>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters.")
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."))
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.")
        consume(LEFT_BRACE, "Expect '{' before function body.")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    // varDecl       → "var" IDENTIFIER ( "=" expression )? ";" ;
    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")
        var initializer: Expr = Expr.Empty
        if (match(EQUAL)) {
            initializer = expression()
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    // statement     → exprStmt
    //               | ifStmt
    //               | printStmt
    //               | whileStmt
    //               | block ;
    private fun statement(): Stmt {
        if (match(IF)) return ifStatement()
        if (match(WHILE)) return whileStatement()
        if (match(FOR)) return forStatement()
        if (match(PRINT)) return printStatement()
        if (match(RETURN)) return returnStatement()
        if (match(BREAK)) return breakStatement()
        if (match(CONTINUE)) return continueStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block()) // <- entry to a new block { ... }
        return expressionStatement()
    }

    // ifStmt        → "if" "(" expression ")" statement
    //               ( "else" statement )? ;
    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    // whileStmt     → "while" "(" expression ")" statement ;
    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")

        try {
            loopDepth += 1

            val body = statement()
            return Stmt.While(condition, body, null)
        } finally {
            loopDepth -= 1
        }
    }

    // forStmt       → "for" "(" ( varDecl | exprStmt | ";" )
    //                 expression? ";"
    //                 expression? ")" statement ;
    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")
        val initializer: Stmt? =
            if (match(SEMICOLON)) {
                null
            } else if (match(VAR)) {
                varDeclaration()
            } else {
                expressionStatement()
            }

        var condition: Expr = Expr.Literal(true) // infinite like the while loop
        if (!check(SEMICOLON)) {
            condition = expression()
        }
        consume(SEMICOLON, "Expect ';' after loop condition.")

        var increment: Expr? = null
        if (!check(RIGHT_PAREN)) {
            increment = expression()
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        // ------------------------------------------------------------

        try {
            loopDepth += 1

            var body = statement()

            // initializer -> condition -> body -> increment
            body = Stmt.While(condition, body, increment)
            if (initializer != null) {
                body = Stmt.Block(listOf(initializer, body))
            }

            return body
        } finally {
            loopDepth -= 1
        }
    }

    // block         → "{" declaration* "}" ;
    private fun block(): List<Stmt> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    // printStmt     → "print" expression ";" ;
    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    // returnStmt    → "return" expression? ";" ;
    private fun returnStatement(): Stmt {
        val keyword = previous() // "return" is already consumed
        var value: Expr? = null
        if (!check(SEMICOLON)) {
            value = expression()
        }
        consume(SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    // breakStmt     → "break" ";" ;
    private fun breakStatement(): Stmt {
        if (loopDepth == 0) {
            error(previous(), "Cannot break outside of loop.")
        }
        consume(SEMICOLON, "Expect ';' after break.")
        return Stmt.Break()
    }

    // continueStmt  → "continue" ";" ;
    private fun continueStatement(): Stmt {
        if (loopDepth == 0) {
            error(previous(), "Cannot continue outside of loop.")
        }
        consume(SEMICOLON, "Expect ';' after continue.")
        return Stmt.Continue()
    }

    // exprStmt      → expression ";" ;
    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    // expression    → assignment ;
    private fun expression(): Expr {
        return assignment()
    }

    // assignment    → IDENTIFIER "=" assignment
    //               | logic_or ;
    private fun assignment(): Expr {
        val expr = or()
        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                return Expr.Assign(expr.name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    // logic_or      → logic_and ( "or" logic_and )* ;
    private fun or(): Expr {
        var expr: Expr = and()
        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    // logic_and     → equality ( "and" equality )* ;
    private fun and(): Expr {
        var expr: Expr = equality()
        while (match(AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    // equality      → comparison ( ( "!=" | "==" ) comparison )* ;
    private fun equality(): Expr {
        var expr: Expr = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator: Token = previous()
            val right: Expr = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // comparison    → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private fun comparison(): Expr {
        var expr: Expr = term()
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right: Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // term          → factor ( ( "-" | "+" ) factor )* ;
    private fun term(): Expr {
        var expr: Expr = factor()
        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right: Expr = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // factor        → unary ( ( "/" | "*" ) unary )* ;
    private fun factor(): Expr {
        var expr: Expr = unary()
        while (match(SLASH, STAR)) {
            val operator = previous()
            val right: Expr = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // unary         → ( "!" | "-" ) unary
    //               | call ;
    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return call()
    }

    // call          → primary ( "(" arguments? ")" )* ;
    private fun call(): Expr {
        var expr: Expr = primary()
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments: MutableList<Expr> = ArrayList()
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(current(), "Cannot have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }
        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")
        return Expr.Call(callee, paren, arguments)
    }

    // primary       → "true" | "false" | "nil"
    //               | NUMBER | STRING
    //               | "(" expression ")"
    //               | IDENTIFIER ;
    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)
        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }
        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }
        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }
        throw error(previous(), "Expect expression.")
    }

    /// Helper Methods -----------------------------------------

    // advance to the next token and synchronize if necessary
    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            // After a semicolon, we’re probably finished with a statement
            if (previous().type === SEMICOLON) return

            // Most statements start with a keyword—for, if, return, var, etc. When the next token
            // is any of those, we’re probably about to start a statement.
            when (current().type) {
                CLASS,
                FUN,
                VAR,
                FOR,
                IF,
                WHILE,
                PRINT,
                RETURN -> return
                else -> {
                    advance()
                }
            }
        }
    }

    // match the given token types and advance if they are found
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    // check if the current token is of the given type
    private fun check(type: TokenType): Boolean {
        return if (isAtEnd()) false else current().type === type
    }

    // advance to the next token and return the previous token (current)
    // this is used to return the previous token to the caller of advance()
    // so that the caller can use it to create a new Expr.Binary() instance
    // with the previous token as the left operand and the current token as the right operand.
    // this is done in the equality() method, which is called recursively.
    private fun advance(): Token {
        if (!isAtEnd()) index++
        return previous()
    }

    // consume the current token if it is of the given type
    // throw an error if the current token is not of the given type
    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(current(), message)
    }

    private fun isAtEnd(): Boolean {
        return current().type === EOF
    }

    // return the current token
    private fun current(): Token {
        return tokens[index]
    }

    // return the previous token
    private fun previous(): Token {
        return tokens[index - 1]
    }

    // return the token at the given index
    private fun peek(lookAhead: Int = 0) = tokens[index + lookAhead]

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }
}
