package low.orbit.kite

import low.orbit.kite.dsl.gemtext
import low.orbit.kite.testcontext.KiteServerExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.nio.charset.StandardCharsets

class Yello {

    @JvmField
    @RegisterExtension
    val server = KiteServerExtension {
        route("/yello") {
            gemtext { +"Howdy, Stranger!" }
        }
    }

    private val client = server.client()

    @Test
    fun test() {
        val response = client.query("/yello")
        assertEquals(
            "20 text/gemini\r\nHowdy, Stranger!\r\n",
            String(response, StandardCharsets.UTF_8)
        )
    }
}
