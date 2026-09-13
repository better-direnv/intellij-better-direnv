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
//    implementation("com.google.code.gson:gson:2.9.0")
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
        bundledPlugins(listOf("com.intellij.java"))
    }
}
