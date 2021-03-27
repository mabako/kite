package low.orbit.kite

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import org.slf4j.LoggerFactory

class GeminiEncoder : MessageToMessageEncoder<GeminiResponse>() {
    private val log = LoggerFactory.getLogger(GeminiEncoder::class.java)

    override fun encode(ctx: ChannelHandlerContext, msg: GeminiResponse, out: MutableList<Any>) {
        log.trace("|> Response: {} {}", msg.status, msg.meta)
        out.add("${msg.status} ${msg.meta}\r\n")

        if (msg is GeminiResponse.Gemtext) {
            out.add(msg.asString())
        }
    }
}
