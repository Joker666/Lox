import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run(
            """
var a = 0;
var temp;

for (var b = 1; a < 10000; b = temp + b) {
    if (a > 1000) {
        break;
    }
    print a;
    temp = a;
    a = b;
}
"""
        )
    }
}
