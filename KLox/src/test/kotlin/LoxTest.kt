import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run(
            """
for (var a = 1; a <= 5; a = a + 1) {
    if (a == 3) {
        continue;
    }
    print a;
}
"""
        )
    }
}
