package low.orbit.kite

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

/**
 * Executes the associates callbacks for gemini requests.
 */
class GeminiRouter(
    private val kiteOptions: KiteOptionsSnapshot
) : SimpleChannelInboundHandler<String>() {

    private val log = LoggerFactory.getLogger(GeminiRouter::class.java)

    /**
     * @param msg since a previous step maps the byte buffer to a String (and there's no multiple headers here),
     *            we can just handle Strings here.
     */
    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        log.trace("Request for {}", msg)

        val url: URL
        try {
            url = URL(msg)
            if (!checkURL(ctx, url)) {
                return
            }
        } catch (e: Exception) {
            log.warn("Unable to handle URL {}", msg, e)
            ctx.close()
            return
        }

        val path = if (url.path == "") "/" else url.path
        val route = kiteOptions.routes.firstOrNull { it.matches(path) } ?: GeminiRoute.NotFound
        ctx.writeAndFlush(
            try {
                route.callback()
            } catch (e: Exception) {
                log.warn("Unable to invoke callback ${route.callback}", e)
                GeminiResponse.temporaryFailure()
            }
        )
            .addListener(ChannelFutureListener.CLOSE)
    }

    /** Check if the URL is somewhat valid. */
    private fun checkURL(ctx: ChannelHandlerContext, url: URL): Boolean {
        if (url.protocol != "gemini") {
            log.warn("Request for {} has invalid protocol, closing channel", url)
            ctx.close()
            return false
        }

        if (url.host != "localhost") {
            log.warn("Request for {} has invalid host, refusing to proxy", url)
            ctx.writeAndFlush(GeminiResponse.proxyResourceRefused())
                .addListener(ChannelFutureListener.CLOSE)
            return false
        }

        val validPort = if (url.port == -1)
            url.defaultPort == kiteOptions.port
        else
            url.port == kiteOptions.port
        if (!validPort) {
            log.warn("Request for {} has invalid port, refusing to proxy", url)
            ctx.writeAndFlush(GeminiResponse.proxyResourceRefused())
                .addListener(ChannelFutureListener.CLOSE)
            return false
        }

        return true
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.warn("Caught unhandled exception handling request", cause)
        ctx.close()
    }

    companion object {
        init {
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
        }
    }
}
