plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation(project(":kite-lib"))
}

application {
    mainClassName = "low.orbit.kite.example.ExampleServer"
}
