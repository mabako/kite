package low.orbit.kite

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.ssl.SniHandler
import io.netty.handler.ssl.SslContext
import io.netty.util.DomainWildcardMappingBuilder
import java.nio.charset.StandardCharsets

class GeminiInitializer(
    private val kiteOptions: KiteOptionsSnapshot,
    private val sslContext: SslContext
): ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
            .addLast(SniHandler(DomainWildcardMappingBuilder(sslContext)
                .add("localhost", sslContext)
                .build()))
            .addLast(LineBasedFrameDecoder(1024 + 2 /* includes crlf */, true, true))
            .addLast(StringDecoder(StandardCharsets.UTF_8))
            .addLast(StringEncoder(StandardCharsets.UTF_8))
            .addLast(GeminiEncoder())
            .addLast(GeminiRouter(kiteOptions))
    }
}
