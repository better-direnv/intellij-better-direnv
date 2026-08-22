pluginManagement {
    val intellijPlatformVersion = providers.gradleProperty("intellijPlatformVersion").get()
    plugins {
        id("org.jetbrains.intellij.platform") version intellijPlatformVersion
    }
}

rootProject.name = "better_direnv"

include(
    "modules/core",
    "modules/products/goland",
    "modules/products/idea",
    "modules/products/nodejs",
    "modules/products/shellscript",
    "modules/products/python",
    "modules/products/phpstorm",
    "modules/products/rubymine"
)

rootProject.children.forEach {
    it.name = (it.name.replaceFirst("modules/", "better_direnv/").replace("/", "-"))
}
