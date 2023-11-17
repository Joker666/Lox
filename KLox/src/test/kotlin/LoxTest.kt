import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run(
            """
for (var a = 1; a <= 5; a = a + 1) {
    if (a == 3) {
        break;
    }
    print a;
}

var i = 1;
while (i <= 5) {
    print i;
    i = i + 1;
}
"""
        )
    }
}
