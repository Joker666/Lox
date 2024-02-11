import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run(
            """
class Bagel {}
var bagel = Bagel();
print bagel; // Prints "Bagel instance".
"""
        )
    }
}
