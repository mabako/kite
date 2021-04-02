buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:10.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.4.31"
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        create("integrationTest") {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }

    val integrationTestImplementation: Configuration by configurations.getting {
        extendsFrom(configurations.testImplementation.get())
    }

    val integrationTestRuntimeOnly: Configuration by configurations.getting {
        extendsFrom(configurations.testRuntimeOnly.get())
    }

    val integrationTest = task<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        shouldRunAfter("test")
    }

    tasks.check { dependsOn(integrationTest) }

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }
}
