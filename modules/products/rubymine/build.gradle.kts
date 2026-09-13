fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.intellij.platform")
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":better_direnv-core"))
    intellijPlatform {
        create("IU", properties("platformVersion"))
        plugins(listOf("org.jetbrains.plugins.ruby:223.7571.4"))
    }
}
