plugins {
    kotlin("jvm") version "1.4.31"
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }
}
