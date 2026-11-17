plugins {
    id("net.fabricmc.fabric-loom") version "1.18-SNAPSHOT"
}

val archivesBaseName = providers.gradleProperty("archives_base_name").get()
val mavenGroup = providers.gradleProperty("maven_group").get()
val modVersion = providers.gradleProperty("mod_version").get()
val jdkVersion = providers.gradleProperty("jdk_version").get()
val minecraftVersion = providers.gradleProperty("minecraft_version").get()
val loaderVersion = providers.gradleProperty("loader_version").get()
val modmenuVersion = providers.gradleProperty("modmenu_version").get()

base {
    archivesName = archivesBaseName
    group = mavenGroup
    version = "$modVersion+$minecraftVersion"
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$loaderVersion")

    // Optional ModMenu integration for in-game config screen
    compileOnly("maven.modrinth:modmenu:$modmenuVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(jdkVersion.toInt()))
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(jdkVersion.toInt())
    }

    jar {
        inputs.property("archivesName", archivesBaseName)
        from("LICENSE") {
            rename { "${it}_$archivesBaseName" }
        }
    }
}
