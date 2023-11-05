import ScannerConstants.ALPHAS
import ScannerConstants.KEYWORDS
import ScannerConstants.NUL
import TokenType.*
import java.lang.Character.isDigit

class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 0

    // scanTokens returns a list of tokens.
    // It processes the source code line by line.
    // It calls scanToken for each line.
    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    // scanToken processes a single token.
    // It advances the current position and adds the token to the list of tokens.
    private fun scanToken() {
        when (val c: Char = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            ' ',
            '\r',
            '\t' -> {} // ignored whitespace
            '\n' -> line++
            '/' -> processSlash()
            '"' -> processStringLiteral()
            else ->
                when {
                    isDigit(c) -> processNumber()
                    isAlpha(c) -> processIdentifier()
                    else -> Lox.error(line, "Unexpected character.")
                }
        }
    }

    // match advances the current position if the current character matches the expected character.
    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    // advance advances the current position and returns the character at the new position.
    private fun advance(): Char {
        return source[current++]
    }

    // current returns the character at the current position without advancing.
    private fun current(): Char {
        return if (isAtEnd()) NUL else source[current]
    }

    // peek returns the next character without advancing.
    private fun peek(): Char {
        return if (current + 1 >= source.length) NUL else source[current + 1]
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    // addToken adds a token to the list of tokens.
    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    // isAtEnd returns true if the current position is at the end of the source.
    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    // isAlpha returns true if the character is a letter or underscore.
    private fun isAlpha(c: Char): Boolean = c in ALPHAS

    // isAlphaNumeric returns true if the character is a letter, digit or underscore.
    private fun isAlphaNumeric(c: Char) = isAlpha(c) || isDigit(c)

    // processStringLiteral processes a string literal.
    // A string literal is wrapped in double quotes.
    // It can contain any character except double quotes.
    private fun processStringLiteral() {
        while (current() != '"' && !isAtEnd()) {
            if (current() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
            return
        }

        // The closing ".
        advance()

        // Trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    // processSlash processes a slash.
    // A slash can either be a comment or a division operator.
    // If it is a comment, it goes until the end of the line.
    // If it is a division operator, it is converted to a token.
    private fun processSlash() {
        if (match('/')) {
            // A comment goes until the end of the line.
            while (current() != '\n' && !isAtEnd()) advance()
        } else {
            addToken(SLASH)
        }
    }

    // processNumber processes a number.
    // A number can contain any number of digits, but must have at least one.
    // It can optionally contain a decimal point.
    // It is converted to a double.
    private fun processNumber() {
        while (isDigit(current())) advance()

        // Look for a fractional part.
        if (current() == '.' && isDigit(peek())) {
            // Consume the "."
            advance()

            while (isDigit(current())) advance()
        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    // processIdentifier processes an identifier.
    // An identifier starts with a letter or underscore and can contain any number of letters,
    // digits, or underscores.
    // It is converted to a keyword if it is a reserved word.
    private fun processIdentifier() {
        while (isAlphaNumeric(current())) advance()

        val text = source.substring(start, current)
        val type = KEYWORDS[text] ?: IDENTIFIER
        addToken(type)
    }
}

object ScannerConstants {
    const val NUL = '\u0000' // Weirdly Kotlin doesn't support '\0' as a char o.O
    val ALPHAS = ('a'..'z').union('A'..'Z').plus('_')
    val KEYWORDS =
        mapOf(
            "and" to AND,
            "class" to CLASS,
            "else" to ELSE,
            "false" to FALSE,
            "for" to FOR,
            "fun" to FUN,
            "if" to IF,
            "nil" to NIL,
            "or" to OR,
            "print" to PRINT,
            "return" to RETURN,
            "super" to SUPER,
            "this" to THIS,
            "true" to TRUE,
            "var" to VAR,
            "while" to WHILE,
            "break" to BREAK,
            "continue" to CONTINUE
        )
}
