package low.orbit.kite

import java.lang.StringBuilder

sealed class GeminiResponse constructor(
    val status: Int,
    val meta: String
) {
    class Gemtext : GeminiResponse(20, MimeType.TEXT_GEMINI.value) {
        private val content: StringBuilder = StringBuilder()

        fun withContent(newContent: String): Gemtext {
            content.clear()
            content.append(newContent)
            return this
        }

        fun line(text: String): Gemtext {
            content.append(text)
                .append("\r\n")
            return this
        }

        operator fun String.unaryPlus() = line(this)
        fun h1(text: String) = line("# $text")
        fun h2(text: String) = line("## $text")
        fun h3(text: String) = line("### $text")
        fun link(link: String, text: String) = line("=> $link $text")
        fun asString() = content.toString()
    }

    private class Error(status: Int, message: String) : GeminiResponse(status, message)

    companion object {
        fun temporaryFailure(message: String = "Temporary Failure"): GeminiResponse = Error(40, message)
        fun unavailable(message: String = "Unavailable"): GeminiResponse = Error(41, message)

        fun permanentFailure(message: String = "Permanent Failure"): GeminiResponse = Error(50, message)
        fun notFound(): GeminiResponse = Error(51, "Not Found")
        fun proxyResourceRefused(): GeminiResponse = Error(53, "Proxy Request refused")
    }
}
