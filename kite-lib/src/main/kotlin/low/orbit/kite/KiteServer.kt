package low.orbit.kite

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files

class KiteServer(options: KiteOptions) {

    private val log = LoggerFactory.getLogger(KiteServer::class.java)
    private val kiteOptions = options.snapshot()
    private var context: Context? = null

    /** Runs the server forever, blocking call. */
    fun run() {
        start()
        context!!.channelFuture.channel().closeFuture().sync()
    }

    fun start() {
        if (context != null)
            throw IllegalStateException("Server context is already initialized, shut down the server first")

        val sslContext = selfSignedCertificate()
            .protocols("TLSv1.3")
            .build()

        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()

        try {
            val bootstrap = ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(GeminiInitializer(kiteOptions, sslContext))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

            val future = bootstrap.bind(kiteOptions.port)
                .sync()
            context = Context(bossGroup, workerGroup, future)
            log.info("Started gemini server on port ${kiteOptions.port}")
        } catch (e: Exception) {
            shutdown()
        }
    }

    fun shutdown() {
        val context = this.context
            ?: throw IllegalStateException("Server context not available, start the server first")

        log.info("Shutting down gemini server on port ${kiteOptions.port}")
        val (bossGroup, workerGroup, future) = context
        workerGroup.shutdownGracefully().sync()
        bossGroup.shutdownGracefully().sync()

        future.channel().closeFuture().sync()

        this.context = null
    }

    private fun selfSignedCertificate(): SslContextBuilder {
        val certificateFile = File("certificate.cert")
        val keyFile = File("certificate.key")

        return try {
            SslContextBuilder.forServer(certificateFile, keyFile)
        } catch (e: Exception) {
            val selfSignedCertificate = SelfSignedCertificate()

            Files.copy(selfSignedCertificate.certificate().toPath(), certificateFile.toPath())
            Files.copy(selfSignedCertificate.privateKey().toPath(), keyFile.toPath())

            return SslContextBuilder.forServer(selfSignedCertificate.certificate(), selfSignedCertificate.privateKey())
        }
    }

    private data class Context(
        val bossGroup: NioEventLoopGroup,
        val workerGroup: NioEventLoopGroup,
        val channelFuture: ChannelFuture
    )
}
