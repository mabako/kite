package low.orbit.kite

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.ssl.SniCompletionEvent
import io.netty.util.AttributeKey
import org.slf4j.LoggerFactory
import java.net.URL

/**
 * Executes the associates callbacks for gemini requests.
 */
class GeminiRouter(
    private val kiteOptions: KiteOptionsSnapshot
) : SimpleChannelInboundHandler<String>() {

    private val log = LoggerFactory.getLogger(GeminiRouter::class.java)

    init {
        UrlProtocolInitializer.init()
    }

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

        val expectedSniHost: String? = ctx.channel().attr(ATTR_SNI_HOSTNAME).get()
        if (expectedSniHost == null) {
            log.trace("No SNI hostname found, skipping comparison")
        } else if (url.host != expectedSniHost) {
            log.warn("SNI lookup was for {}, but request url is for {}", expectedSniHost, url.host)
            ctx.writeAndFlush(GeminiResponse.permanentFailure("SNI Lookup Failure"))
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

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is SniCompletionEvent) {
            log.trace("SNI Host name resolved to {}", evt.hostname())
            ctx.channel().attr(ATTR_SNI_HOSTNAME).set(evt.hostname())
        }
        super.userEventTriggered(ctx, evt)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.warn("Caught unhandled exception handling request", cause)
        ctx.close()
    }

    companion object {
        private val ATTR_SNI_HOSTNAME = AttributeKey.valueOf<String>(GeminiRouter::class.java, "sni-hostname")
    }
}
