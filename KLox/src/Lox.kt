import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.system.exitProcess

object Lox {
    private var hadError: Boolean = false
    private var hadRuntimeError = false

    private val interpreter = Interpreter()

    @Throws(IOException::class)
    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    @Throws(IOException::class)
    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        // Indicate an error in the exit code.
        if (hadError) exitProcess(ExitCodes.DATA_ERR.value)
        if (hadRuntimeError) exitProcess(ExitCodes.SOFTWARE.value)
    }

    fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        val parser = Parser(tokens)
        val statements = parser.parse()

        // Stop if there was a syntax error.
        if (hadError) return

        val resolver = Resolver(interpreter)
        resolver.resolve(statements)

        // Stop if there was a resolution error.
        if (hadError) return

        interpreter.interpret(statements)
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type === TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message)
        }
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println(
            """
            ${error.message}
            [line ${error.token.line}]
            """
                .trimIndent()
        )
        hadRuntimeError = true
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }
}
