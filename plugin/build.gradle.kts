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
}

version = "1.0.0-SNAPSHOT"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven {
        name = "Creeperhost"
        url = uri("https://maven.creeperhost.net/")
    }
}

dependencies {
    implementation("net.fabricmc:fabric-loom:1.3-SNAPSHOT")
    implementation("net.minecraftforge.gradle:ForgeGradle:6.0.+")
    // Use JUnit test framework for unit tests
    testImplementation("junit:junit:4.13.1")
}

gradlePlugin {
    // Define the plugin
    val mlp by plugins.creating {
        id = "dev.imabad.mlp"
        implementationClass = "dev.imabad.mlp.MultiLoaderPlugin"
    }
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
