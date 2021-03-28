package low.orbit.kite

class KiteOptions(
    override val routes: MutableList<GeminiRoute> = mutableListOf(),
    override var port: Int = DEFAULT_PORT
) : KiteOptionsSnapshot {

    /** Adds a route. */
    fun route(path: String, callback: () -> GeminiResponse) {
        routes.add(GeminiRoute.Exact(path, callback))
    }

    /** Converts the current options into a snapshot. */
    fun snapshot() = let { source ->
        object : KiteOptionsSnapshot {
            override val routes = source.routes.toList()
            override val port = source.port
        }
    }

    companion object {
        const val DEFAULT_PORT = 1965
    }
}
