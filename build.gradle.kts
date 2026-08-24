import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "dev.ludwigbrida"
version = "0.0.0"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        webstorm("2026.2.1")
    }
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "262"
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    buildPlugin {
        archiveFileName = "wgsl-analyzer.zip"
        destinationDirectory = layout.projectDirectory.dir("dist")
    }

    named("build") {
        dependsOn(buildPlugin)
    }

    withType<RunIdeTask> {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf("-Didea.log.debug.categories=#com.intellij.platform.lsp")
        }
    }
}
