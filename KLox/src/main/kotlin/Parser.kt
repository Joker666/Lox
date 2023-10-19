import TokenType.*

internal class Parser(private val tokens: List<Token>) {
    private class ParseError : RuntimeException()

    private var index = 0

    fun parse(): List<Stmt?> {
        val statements = mutableListOf<Stmt?>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    // declaration   → varDecl
    //               | statement ;
    private fun declaration(): Stmt? {
        return try {
            if (match(VAR)) varDeclaration() else statement()
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    // varDecl       → "var" IDENTIFIER ( "=" expression )? ";" ;
    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer!!)
    }

    // statement     → exprStmt
    //               | printStmt ;
    private fun statement(): Stmt {
        return if (match(PRINT)) printStatement() else expressionStatement()
    }

    // printStmt     → "print" expression ";" ;
    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    // exprStmt      → expression ";" ;
    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    // expression    → equality ;
    private fun expression(): Expr {
        return equality()
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
    //               | primary ;
    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return primary()
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

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }
}
