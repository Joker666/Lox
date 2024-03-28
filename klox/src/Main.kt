import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when (args.size) {
        0 -> Lox.runPrompt()
        1 -> Lox.runFile(args[0])
        else -> {
            println("Usage: klox [script]")
            exitProcess(64)
        }
    }
}
