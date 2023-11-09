/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Gradle plugin project to get you started.
 * For more details take a look at the Writing Custom Plugins chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.0.2/userguide/custom_plugins.html
 */

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.0"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

version = "0.1.0"
group = "dev.nanite"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "Creeperhost"
        url = uri("https://maven.creeperhost.net/")
    }
}

dependencies {
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("net.fabricmc:tiny-remapper:0.8.7")
    implementation ("net.fabricmc:access-widener:2.1.0")
    implementation ("net.fabricmc:mapping-io:0.2.1")

    implementation("net.fabricmc:fabric-loom:1.4-SNAPSHOT")
    implementation("net.minecraftforge.gradle:ForgeGradle:6.0.+")
    implementation("org.spongepowered:mixingradle:0.7.+")
    implementation("net.neoforged.gradle:userdev:7.0.33")
    // Use JUnit test framework for unit tests
    testImplementation("junit:junit:4.13.1")
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}


gradlePlugin {
    website = "https://github.com/nanite/multiloader-plugin/"
    vcsUrl = "https://github.com/nanite/multiloader-plugin/"
    testSourceSet(sourceSets["test"])

    plugins.create("mlp") {
        id = "dev.nanite.mlp"
        implementationClass = "dev.nanite.mlp.MultiLoaderPlugin"
        displayName = "MultiLoader Plugin"
        description = "A gradle plugin designed for easing the creation of MultiLoader Minecraft mods. It currently supports Fabric & Forge."
        version = project.version
        tags = listOf("minecraft")
    }
}

publishing {
    publications {
        create<MavenPublication>("snapshot") {
            groupId = "dev.nanite.mlp"
            artifactId = "dev.nanite.mlp.gradle.plugin"
            version = "${project.version}-SNAPSHOT"
            from(components["java"])
        }
    }
    repositories {
        if (providers.environmentVariable("NANITE_TOKEN").isPresent()) {
            maven {
                url = uri("https://maven.nanite.dev/snapshots")
                credentials {
                    username = "nanite"
                    password = providers.environmentVariable("NANITE_TOKEN").get()
                }
            }
        }
    }
}