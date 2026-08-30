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
    // Compiled against the oldest Paper API we support, not the newest.
    // This is what lets the built jar run unmodified on 1.21.x through
    // whatever the current release is — Paper's API is additive, so code
    // written against 1.21.4 keeps working on newer servers.
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
