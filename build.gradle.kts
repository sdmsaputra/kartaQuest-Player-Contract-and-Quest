import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version "1.9.23"
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.run-paper") version "2.2.4"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.minekarta.karta"
version = "3.0.0-SNAPSHOT" // Bumping version for the major refactor

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io") // Vault
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
}

dependencies {
    // Paper API (provides Bukkit, Spigot, and Paper APIs)
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")

    // Kotlin Stdlib
    implementation(kotlin("stdlib"))

    // Kyori Adventure for modern text components
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.3")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")

    // HikariCP for database connection pooling & SQLite driver
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")

    // Integrations (compileOnly since they are provided by other plugins)
    compileOnlyApi("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")

    // bStats for metrics - shaded into the final jar
    implementation("org.bstats:bstats-bukkit:3.0.2")

    // JSON library for serialization
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks {
    // Configure paperweight
    reobfJar {
        // In case we want to publish both reobf and non-reobf jars
        // outputJar.set(project.layout.buildDirectory.file("libs/${project.name}-${project.version}-reobf.jar"))
    }

    assemble {
        dependsOn(reobfJar)
    }

    // Configure Java and Kotlin compilation
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    // Configure runServer task for local testing
    runServer {
        minecraftVersion("1.21")
    }

    shadowJar {
        val bstatsPath = "org.bstats"
        relocate(bstatsPath, "${project.group}.${project.name.lowercase()}.lib.$bstatsPath")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

// Shading bStats
tasks.reobfJar {
    // This is now handled by the shadowJar task
}
