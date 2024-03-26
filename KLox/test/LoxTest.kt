import org.junit.jupiter.api.Test

class LoxTest {
    @Test
    fun run() {
        Lox.run(
            """
class Cake {
  taste() {
    var adjective = "delicious";
    print "The " + this.flavor + " cake is " + adjective + "!";
  }
}

var cake = Cake();
cake.flavor = "German chocolate";
cake.taste(); // Prints "The German chocolate cake is delicious!".
"""
        )
    }
}
