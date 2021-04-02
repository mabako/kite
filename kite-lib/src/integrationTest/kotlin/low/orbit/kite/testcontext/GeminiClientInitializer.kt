package low.orbit.kite.testcontext

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.bytes.ByteArrayDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.ssl.SslContext
import java.net.URL

internal class GeminiClientInitializer(
    private val sslContext: SslContext,
    private val url: URL
) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        val serverPort = if (url.port == -1) url.defaultPort else url.port

        ch.pipeline()
            .addLast(sslContext.newHandler(ch.alloc(), url.host, serverPort))
            .addLast(StringEncoder())
            .addLast(ByteArrayDecoder())
            .addLast(GeminiClientHandler())
    }
}
