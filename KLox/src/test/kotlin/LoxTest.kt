import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run("""
var b;

print b; // Error!
""")
    }
}
