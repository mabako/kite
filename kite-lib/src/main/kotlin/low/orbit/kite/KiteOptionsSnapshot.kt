package low.orbit.kite

/** Snapshot of the [KiteOptions] after initialization. */
interface KiteOptionsSnapshot {

    /** Currently mapped routes. */
    val routes: List<GeminiRoute>

    /** Server port. */
    val port: Int
}
