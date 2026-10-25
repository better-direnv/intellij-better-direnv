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
        create("CL", properties("platformVersion"))
        bundledPlugins(listOf("com.intellij.clion", "com.intellij.cidr.lang"))
    }
}
