package low.orbit.kite

sealed class GeminiRoute(
    val callback: () -> GeminiResponse
) {
    abstract fun matches(geminiUrl: String): Boolean

    class Exact(
        private val exactRoute: String,
        callback: () -> GeminiResponse
    ) : GeminiRoute(callback) {

        init {
            require(exactRoute.startsWith("/")) {
                "Path '$exactRoute' needs to start with /"
            }
        }

        override fun matches(geminiUrl: String) =
            geminiUrl == exactRoute
    }

    object NotFound : GeminiRoute(GeminiResponse.Companion::notFound) {
        override fun matches(geminiUrl: String) =
            true
    }
}
