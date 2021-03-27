package low.orbit.kite.example

import low.orbit.kite.GeminiResponse
import low.orbit.kite.KiteServer
import low.orbit.kite.dsl.options
import low.orbit.kite.dsl.gemtext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    KiteServer(options {
        route("/hello.gmi") {
            gemtext {
                h3("Oh, I didn't see you there")
                +"Good day to you!"
            }
        }

        route("/time.gmi", ::printTime)

        route("/fail.gmi") {
            GeminiResponse.permanentFailure("Custom permanent failure")
        }

        route("/") {
            gemtext {
                h1("\uD83E\uDE81 Kite")
                h2("A Kotlin Gemini server")
                +""
                +"This is the default page, aww."
                +""
                link("/hello.gmi", "Hello!")
                link("/time.gmi", "What's the time again?")
                link("/fail.gmi", "This probably isn't available")
            }
        }
    })
    .run()
}

fun printTime() : GeminiResponse =
    GeminiResponse.Gemtext()
        .withContent("It is currently ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))}.")
