import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run("""
var a = 1;
var b = 2;
print a + b;
    """)
    }
}
