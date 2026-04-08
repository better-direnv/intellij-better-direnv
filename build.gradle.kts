import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij.platform")
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.4.0"
    id("org.sonarqube") version "5.1.0.4882"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Root project repositories
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

allprojects {
    version = version
    group = group

    apply(plugin = "java")

    repositories {
        mavenCentral()
    }
}

subprojects {
    pluginManager.withPlugin("org.jetbrains.intellij.platform") {
        extensions.configure<org.jetbrains.intellij.platform.gradle.extensions.IntelliJPlatformExtension> {
            buildSearchableOptions = false
        }
    }
}


dependencies {
    implementation(project(":better_direnv-products-goland"))
    implementation(project(":better_direnv-products-idea"))
    implementation(project(":better_direnv-products-nodejs"))
    implementation(project(":better_direnv-products-shellscript"))
    implementation(project(":better_direnv-products-python"))
    implementation(project(":better_direnv-products-phpstorm"))
    implementation(project(":better_direnv-products-rubymine"))
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        name = properties("pluginName")

        // Configure sinceBuild and untilBuild - explicitly set to maintain compatibility range
        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }

    pluginVerification {
        ides {

            // Some products are disabled, a full test exceeds the disk space of the github runner
            select {
                types = listOf(
//                    IntelliJPlatformType.IntellijIdeaCommunity,
                    IntelliJPlatformType.IntellijIdeaUltimate,
//                    IntelliJPlatformType.PyCharmCommunity,
//                    IntelliJPlatformType.PhpStorm,
//                    IntelliJPlatformType.RubyMine,
//                    IntelliJPlatformType.GoLand,
//                    IntelliJPlatformType.WebStorm
                )

                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = properties("pluginSinceBuild")
                untilBuild = properties("pluginUntilBuild")
            }
        }
    }

    publishing {
        token = providers.gradleProperty("intellijPlatformPublishingToken")
        channels = listOf("beta")
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

sonarqube {
    properties {
        property("sonar.projectKey", "Fapiko_intellij-better-direnv")
        property("sonar.organization", "fapiko")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

gradle.taskGraph.whenReady(closureOf<TaskExecutionGraph> {
    val ignoreSubprojectTasks = listOf(
        "buildSearchableOptions", "prepareJarSearchableOptions", "listProductsReleases", "patchPluginXml", "publishPlugin", "runIde", "runPluginVerifier",
        "verifyPlugin"
    )

    // Don't run some tasks for subprojects
    for (task in allTasks) {
        if (task.project != task.project.rootProject) {
            when (task.name) {
                in ignoreSubprojectTasks -> task.enabled = false
            }
        }
    }
})

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version = properties("pluginVersion")
        // Note: sinceBuild and untilBuild are now configured in pluginConfiguration.ideaVersion block

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(
            changelog.renderItem(
                changelog.getOrNull(properties("pluginVersion")) ?: changelog.getLatest(),
                org.jetbrains.changelog.Changelog.OutputType.HTML
            )
        )
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
        task {
            jvmArgumentProviders += CommandLineArgumentProvider {
                listOf(
                    "-Drobot-server.port=8082",
                    "-Dide.mac.message.dialogs.as.sheets=false",
                    "-Djb.privacy.policy.text=<!--999.999-->",
                    "-Djb.consents.confirmation.enabled=false",
                )
            }
        }

        plugins {
            robotServerPlugin()
        }
    }

    signPlugin {
        val chainFile = File("chain.crt")
        val keyFile = File("private.pem")
        if (chainFile.exists() && keyFile.exists()) {
            certificateChain.set(chainFile.readText(Charsets.UTF_8))
            privateKey.set(keyFile.readText(Charsets.UTF_8))
            password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
        }
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = System.getenv("PUBLISH_TOKEN")
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
