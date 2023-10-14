import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run("1 + 2 / 3 * 4")
    }
}
