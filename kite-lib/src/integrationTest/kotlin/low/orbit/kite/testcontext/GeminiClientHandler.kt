package low.orbit.kite.testcontext

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

internal class GeminiClientHandler : SimpleChannelInboundHandler<ByteArray>(false) {
    /** Since the Gemini protocol spec does not support multiple connections, we just merge the entire response */
    var receivedBytes = ByteArray(0)
        private set

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteArray) {
        receivedBytes += msg
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        throw IllegalStateException("Error while querying: ${cause.message}", cause)
    }
}
