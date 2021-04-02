package low.orbit.kite.testcontext

import low.orbit.kite.KiteOptions
import low.orbit.kite.KiteServer
import low.orbit.kite.dsl.options
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.random.Random

/**
 * Wrapper for a random-port based [KiteServer] that is started and stopped for each test executed.
 */
class KiteServerExtension(val init: (KiteOptions.() -> Unit)? = null) : BeforeEachCallback, AfterEachCallback {
    private val port = Random.nextInt(49000, 53000)
    private val server = KiteServer(
        options {
            port = this@KiteServerExtension.port
            init?.invoke(this)
        }
    )

    override fun beforeEach(context: ExtensionContext) {
        server.start()
    }

    override fun afterEach(context: ExtensionContext) {
        Thread.sleep(1000)
        server.shutdown()
    }

    /**
     * Returns a client configured to access the tested server instance.
     */
    fun client() = GeminiClient("gemini://localhost:$port")
}
