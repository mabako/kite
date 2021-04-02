package low.orbit.kite.testcontext

import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import low.orbit.kite.UrlProtocolInitializer
import tlschannel.ClientTlsChannel
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLContext

/**
 * Testable Gemini client, based on [tls-channel](https://github.com/marianobarrios/tls-channel).
 *
 * Netty doesn't support client-side SNI (at least on systems where OpenSSL isn't available, and the JDK handles SSL),
 * which is terrible when Gemini clients should use SNI.
 */
class GeminiClient(
    private val baseAddress: String,
) {
    private val baseURL = URL(baseAddress)
    private val serverPort = if (baseURL.port == -1) baseURL.defaultPort else baseURL.port
    private val sslEngine =
        SSLContext.getInstance(protocol).apply {
            init(null, InsecureTrustManagerFactory.INSTANCE.trustManagers, null)
        }.createSSLEngine(baseURL.host, serverPort).apply {
            useClientMode = true
            enabledProtocols = arrayOf(protocol)
            sslParameters = sslParameters.apply {
                serverNames = listOf(SNIHostName(baseURL.host))
            }
        }

    fun query(absolutePath: String): ByteArray {
        require(absolutePath.startsWith("/")) { "Path needs to start with /" }
        val url = URL("$baseAddress$absolutePath")

        val out = ByteArrayOutputStream()
        SocketChannel.open().use { rawChannel ->
            rawChannel.connect(InetSocketAddress(url.host, serverPort))

            ClientTlsChannel.newBuilder(rawChannel, sslEngine).build().use { tlsChannel ->
                // send our url, then just wait for the response
                tlsChannel.write(ByteBuffer.wrap("$url\r\n".toByteArray(StandardCharsets.UTF_8)))

                // since ByteBuffer.get(ByteArray) just copies one byte at a time from the backing array to the
                // passed-in ByteArray, using the underlying buffer directly mitigates that.
                val backingArray = ByteArray(allocationSize)
                val buffer = ByteBuffer.wrap(backingArray)
                while (tlsChannel.read(buffer) >= 0) {
                    buffer.flip()

                    // As a consequence of reusing the underlying byte array, we don't need to worry about partial reads
                    // when the array passed to ByteBuffer.get(ByteArray) is smaller than the number of remaining bytes.
                    out.write(backingArray, 0, buffer.remaining())
                    buffer.position(buffer.position() + buffer.remaining())
                    assert(!buffer.hasRemaining())

                    // Discard everything in the current buffer
                    buffer.compact()
                    assert(buffer.position() == 0)
                }
            }
        }
        return out.toByteArray()
    }

    companion object {
        private const val protocol: String = "TLSv1.2"
        private const val allocationSize = 64 * 1024

        init {
            UrlProtocolInitializer.init()
        }
    }
}
