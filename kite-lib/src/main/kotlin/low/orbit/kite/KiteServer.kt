package low.orbit.kite

import io.netty.bootstrap.ServerBootstrap
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

    fun run() {
        val sslContext = selfSignedCertificate()
            .protocols("TLSv1.3", "TLSv1.2")
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
            log.info("Started gemini server on port ${kiteOptions.port}")

            future.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
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
}
