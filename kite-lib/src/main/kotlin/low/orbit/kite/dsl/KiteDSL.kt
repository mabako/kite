@file:JvmName("KiteDSL")

package low.orbit.kite.dsl

import low.orbit.kite.GeminiResponse
import low.orbit.kite.KiteOptions

// this is a weird place to put things.

fun options(init: KiteOptions.() -> Unit) =
    KiteOptions()
        .also(init)


fun gemtext(init: GeminiResponse.Gemtext.() -> Unit) =
    GeminiResponse.Gemtext().also(init)
