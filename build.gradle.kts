import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "org.geysermc.extension.emoteoffhand"
version = "1.0.0"

configurations {
    create("shadowBundle") {
        isCanBeResolved = true
        isCanBeConsumed = false
        isTransitive = false
    }
}

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/main")
}

val shadowBundle = configurations.named("shadowBundle")

dependencies {
    compileOnly("org.geysermc.geyser:api:2.9.0-SNAPSHOT")

    implementation("org.yaml:snakeyaml:2.5")
    shadowBundle("org.yaml:snakeyaml:2.5")
}

tasks {
    named<Jar>("jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        archiveFileName.set("${project.name}-${project.version}-nonrelocated.jar")

        from({
            configurations.runtimeClasspath.get().map { file ->
                if (file.isDirectory) file else zipTree(file)
            }
        }, project.rootProject.file("LICENSE"))
    }

    val shadowJar = named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.getByName("shadowBundle"))

        archiveFileName.set("${project.name}.jar")

        relocate("org.yaml.snakeyaml", "org.geysermc.extension.emoteoffhand.shadow.snakeyaml")
    }

    named("build") {
        dependsOn(shadowJar)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
