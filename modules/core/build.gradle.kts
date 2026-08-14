fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform")
    // Gradle Lombok plugin - updated to 8.11 for Java 21 compatibility
    id("io.freefair.lombok") version "9.5.0"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
    }
}
