abstract class Statement {
    class Expression(val expression: Expr) : Statement()

    class Assignment(val variable: String, val expression: String) : Statement()

    class If(val expression: String, val statements: List<Statement>) : Statement()

    class While(val expression: String, val statements: List<Statement>) : Statement()

    class For(val variable: String, val expression: String, val statements: List<Statement>) :
        Statement()

    class Return(val expression: String) : Statement()

    class Break : Statement()

    class Continue : Statement()

    class Print(val expression: Expr) : Statement()

    class Println(val expression: String) : Statement()

    class Printf(val format: String, val expressions: List<String>) : Statement()

    class Error(val message: String) : Statement()

    class Function(
        val name: String,
        val parameters: List<String>,
        val statements: List<Statement>
    ) : Statement()

    class FunctionCall(val name: String, val parameters: List<String>) : Statement()

    class Variable(val name: String) : Statement()
}
