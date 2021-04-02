package low.orbit.kite

import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

object UrlProtocolInitializer {
    private var initialized = false

    fun init() {
        if (initialized)
            return

        URL.setURLStreamHandlerFactory { proto ->
            if (proto == "gemini") {
                object : URLStreamHandler() {
                    override fun openConnection(u: URL?): URLConnection {
                        error("Can't open gemini connections, just need this for URL protocol support")
                    }

                    override fun getDefaultPort() = KiteOptions.DEFAULT_PORT
                }
            } else {
                null
            }
        }

        initialized = true
    }
}
