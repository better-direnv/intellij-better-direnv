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
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
        bundledPlugins(listOf("com.jetbrains.sh"))
    }
}
