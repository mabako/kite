package low.orbit.kite.testcontext

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import low.orbit.kite.UrlProtocolInitializer
import java.net.URL

/**
 * Testable gemini client.
 */
class GeminiClient(private val baseAddress: String) {

    /** Sends a query, returns a byte-array in response. */
    fun query(absolutePath: String): ByteArray {
        require(absolutePath.startsWith("/")) {
            "Path needs to start with /"
        }

        val url = URL("$baseAddress$absolutePath")
        val sslContext = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build()

        val eventGroup = NioEventLoopGroup()

        try {
            val bootstrap = Bootstrap()
                .group(eventGroup)
                .channel(NioSocketChannel::class.java)
                .handler(GeminiClientInitializer(sslContext, url))

            val channel = bootstrap.connect(url.host, url.port).sync().channel()
            val handler = channel.pipeline().get(GeminiClientHandler::class.java)
            channel.writeAndFlush("$url\r\n").sync()
            channel.closeFuture().sync()

            return handler.receivedBytes
        } finally {
            eventGroup.shutdownGracefully()
        }
    }

    companion object {
        init {
            UrlProtocolInitializer.init()
        }
    }
}
