import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run("""
var a = 1;
a = 2;
print a;
""")
    }
}
