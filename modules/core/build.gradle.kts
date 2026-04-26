fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform")
    // Gradle Lombok plugin - updated to 8.11 for Java 21 compatibility
    id("io.freefair.lombok") version "8.11"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
    }
}
