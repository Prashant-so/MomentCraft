plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.5"
}

group = "dev.momentcraft"
version = "0.1.0-SNAPSHOT"
description = "Automated cinematic moment detection and rendering for Paper servers."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set("MomentCraft")
    }

    build {
        dependsOn(shadowJar)
    }
}
