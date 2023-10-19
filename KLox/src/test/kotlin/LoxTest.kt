import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run("""
print "one";
print true;
print 2 + 1;
    """)
    }
}
