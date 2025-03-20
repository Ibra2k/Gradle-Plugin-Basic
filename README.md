# Building a Kotlin Gradle Plugin: A Step-by-Step Guide

This guide walks you through creating a custom Gradle plugin in Kotlin from scratch, including potential pitfalls and solutions.

## Project Overview

We'll build a Gradle plugin that provides two utility tasks:
1. **countLinesOfCode**: Counts lines of code in project files, optionally filtered by extension
2. **copyFilesWithSuffix**: Copies files with a specific suffix to a target directory

## Step 1: Project Structure Setup

First, create the basic directory structure:

```bash
# Create project directories
mkdir -p code-utility-plugin/src/main/kotlin/com/example/plugin
mkdir -p code-utility-plugin/src/main/resources/META-INF/gradle-plugins
mkdir -p example-project/src/main/kotlin/com/example
mkdir -p example-project/src/main/resources
```

## Step 2: Root Project Configuration

Create the root configuration files:

1. **settings.gradle.kts**:
```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "code-utility-gradle-plugin"

// Define plugin as an included build for the example project to use
includeBuild("code-utility-plugin")

// Include the example project
include("example-project")
```

2. **build.gradle.kts**:
```kotlin
plugins {
    kotlin("jvm") version "2.0.21" apply false
}
```

## Step 3: Plugin Project Implementation

1. **code-utility-plugin/build.gradle.kts**:
```kotlin
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("codeUtilityPlugin") {
            id = "com.example.code-utility"
            implementationClass = "com.example.plugin.CodeUtilityPlugin"
        }
    }
}

// Set JVM compatibility
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// Handle duplicates in resources
tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
```

2. **Plugin Implementation Class** (code-utility-plugin/src/main/kotlin/com/example/plugin/CodeUtilityPlugin.kt):
```kotlin
package com.example.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class CodeUtilityPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Register the Line Counter task
        project.tasks.register("countLinesOfCode", CountLinesOfCodeTask::class.java) {
            group = "code utility"
            description = "Counts lines of code in the project"
        }
        
        // Register the Copy Files task
        project.tasks.register("copyFilesWithSuffix", CopyFilesWithSuffixTask::class.java) {
            group = "code utility"
            description = "Copies files with a given suffix to a destination directory"
        }
    }
}
```

3. **CountLinesOfCodeTask** (code-utility-plugin/src/main/kotlin/com/example/plugin/CountLinesOfCodeTask.kt):
```kotlin
package com.example.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class CountLinesOfCodeTask : DefaultTask() {
    
    @get:Input
    @get:Optional
    @get:Option(option = "fileExtension", description = "File extension to count (e.g., 'kt', 'java', without dot)")
    var fileExtension: String? = null
    
    @get:InputFiles
    val sourceFiles: FileTree = project.fileTree(project.projectDir) {
        include("**/*")
        exclude("**/build/**")
        exclude("**/.gradle/**")
        exclude("**/out/**")
        exclude("**/.git/**")
    }
    
    @TaskAction
    fun countLines() {
        var totalLines = 0
        var affectedFiles = 0
        
        val filesToProcess = if (fileExtension != null) {
            sourceFiles.filter { it.extension == fileExtension }
        } else {
            sourceFiles
        }
        
        filesToProcess.forEach { file ->
            if (file.isDirectory) return@forEach
            
            val lines = file.readLines().size
            totalLines = totalLines + lines // Explicitly using "+" operator to avoid ambiguity
            affectedFiles++
            logger.info("${file.relativeTo(project.projectDir)}: $lines lines")
        }
        
        val extensionInfo = fileExtension?.let { ".$it " } ?: ""
        logger.quiet("Total lines of ${extensionInfo}code: $totalLines in $affectedFiles files")
    }
}
```

4. **CopyFilesWithSuffixTask** (code-utility-plugin/src/main/kotlin/com/example/plugin/CopyFilesWithSuffixTask.kt):
```kotlin
package com.example.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class CopyFilesWithSuffixTask : DefaultTask() {
    
    @get:Input
    @get:Option(option = "suffix", description = "Suffix to match for files to copy")
    abstract val suffix: Property<String>
    
    @get:OutputDirectory
    @get:Option(option = "destinationDir", description = "Directory to copy files to")
    abstract val destinationDir: DirectoryProperty
    
    @get:InputFiles
    val sourceFiles: FileTree = project.fileTree(project.projectDir) {
        include("**/*")
        exclude("**/build/**")
        exclude("**/.gradle/**")
        exclude("**/out/**")
        exclude("**/.git/**")
    }
    
    @TaskAction
    fun copyFiles() {
        if (!suffix.isPresent) {
            logger.error("Suffix parameter is required")
            return
        }
        
        if (!destinationDir.isPresent) {
            logger.error("Destination directory parameter is required")
            return
        }
        
        val targetDir = destinationDir.get().asFile
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        
        var copiedCount = 0
        val suffixValue = suffix.get()
        
        sourceFiles.filter { file ->
            file.name.endsWith(suffixValue, ignoreCase = true)
        }.forEach { file ->
            val targetFile = File(targetDir, file.name)
            file.copyTo(targetFile, overwrite = true)
            logger.info("Copied ${file.name} to ${targetFile.absolutePath}")
            copiedCount++
        }
        
        logger.quiet("Copied $copiedCount files with suffix '$suffixValue' to ${targetDir.absolutePath}")
    }
    
    init {
        // Set default values
        destinationDir.convention(project.layout.buildDirectory.dir("copied-files"))
    }
}
```

5. **Plugin Properties** (code-utility-plugin/src/main/resources/META-INF/gradle-plugins/com.example.code-utility.properties):
```properties
implementation-class=com.example.plugin.CodeUtilityPlugin
```

## Step 4: Example Project Setup

1. **example-project/settings.gradle.kts**:
```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("../code-utility-plugin")
}

rootProject.name = "example-project"
```

2. **example-project/build.gradle.kts**:
```kotlin
plugins {
    kotlin("jvm") version "2.0.21"
    id("com.example.code-utility") version "1.0.0"
    application
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

application {
    mainClass.set("com.example.MainKt")
}

// Set JVM compatibility
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
```

3. **example-project/src/main/kotlin/com/example/Main.kt**:
```kotlin
package com.example

fun main() {
    println("Hello from the example project!")
}

class Example {
    fun doSomething() {
        println("Doing something...")
    }
}
```

4. **example-project/src/main/resources/sample.txt**:
```
This is a sample text file.
It will be used to demonstrate the copyFilesWithSuffix task.
```

## Step 5: Initialize Gradle Wrapper

Run this command in the root directory:
```bash
gradle wrapper
```

## Step 6: Running the Plugin Tasks

From the example project directory:

1. Count lines of code:
```bash
# Count all lines of code
../gradlew countLinesOfCode

# Count lines of code for specific file extension
../gradlew countLinesOfCode --fileExtension=kt
```

2. Copy files with suffix:
```bash
# Copy all .txt files
../gradlew copyFilesWithSuffix --suffix=.txt

# Specify a custom destination directory
../gradlew copyFilesWithSuffix --suffix=.txt --destinationDir=build/custom-dir
```

3. Run the example application:
```bash
../gradlew run
```

## Common Issues and Troubleshooting

### 1. JVM Target Compatibility Issues

**Problem**: You might see errors like:
```
Inconsistent JVM-target compatibility detected for tasks 'compileJava' (21) and 'compileKotlin' (11).
```

**Solution**: Ensure your Java and Kotlin compiler targets match by adding proper toolchain configuration:
```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
```

### 2. Plugin Resolution Issues

**Problem**: Example project can't find your plugin.

**Solution**: Make sure you:
1. Use `includeBuild("../code-utility-plugin")` in your example project's settings.gradle.kts
2. Have matching plugin IDs between your plugin implementation and usage

### 3. Duplicate Resources

**Problem**: Errors about duplicate entries in META-INF.

**Solution**: Add duplicates strategy to your plugin's build file:
```kotlin
tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
```

### 4. Gradle Wrapper Missing

**Problem**: Can't run `./gradlew` commands because the wrapper scripts don't exist.

**Solution**: Initialize the Gradle wrapper in your project:
```bash
gradle wrapper
```

### 5. Kotlin Gradle DSL Deprecation Warnings

**Problem**: Warnings about kotlinOptions being deprecated.

**Solution**: Use the new compilerOptions DSL:
```kotlin
// Old way (deprecated)
kotlinOptions.jvmTarget = "11"

// New way
compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
}
```

## Publishing Your Plugin

For local testing, publish to your local Maven repository:
```bash
cd code-utility-plugin
../gradlew publishToMavenLocal
```

For production use, publish to the Gradle Plugin Portal:
1. Register on https://plugins.gradle.org/
2. Configure your plugin for publishing
3. Run `../gradlew publishPlugins`

## Conclusion

Building a Gradle plugin in Kotlin can be challenging but is also rewarding. The most important things to watch out for are:

1. Proper project structure with directories and properties files in the right places
2. JVM compatibility between Java and Kotlin
3. Using the correct version of Kotlin and Gradle
4. Initializing the Gradle wrapper
5. Using `includeBuild` for composite builds during testing

With these considerations in mind, you can create powerful Gradle plugins that enhance build processes and automate common tasks. 
