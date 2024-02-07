import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run(
            """
class DevonshireCream {
  serveOn() {
    return "Scones";
  }
}

print DevonshireCream; // Prints "DevonshireCream".
"""
        )
    }
}
