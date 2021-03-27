plugins {
    kotlin("jvm") version "1.4.31"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.netty:netty-all:4.1.60.Final")
    implementation("org.slf4j:slf4j-api:1.7.25")
}
