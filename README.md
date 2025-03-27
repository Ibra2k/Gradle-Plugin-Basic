# Custom Gradle Plugin

A custom Gradle plugin with two tasks: one to count lines of Kotlin code and another to create a folder with a text file.

## Features

1. Count Lines of Kotlin Code
2. Counts non-blank lines in .kt files within src/main/kotlin/com/example/. Supports:

- Counting lines for all Kotlin files in the directory (default).

- Counting lines for a specific file using the --file option.


1. Create Folder and Text File
2. Creates a folder named plugin-folder and a text file inside it. Supports:

- Creating a default.txt file (default).

- Creating a custom-named file using the --addTxt option.


##  Installation
- Add the Plugin to Your Project

Ensure the plugin code is in your project's buildSrc directory (or include it via your preferred method).
Apply the plugin in your build.gradle or build.gradle.kts:

```
plugins {
    id("com.example.plugin")
}
```

## Usage

### Task 1: countKtLines

Counts lines of Kotlin code.

#
`./gradlew countKtLines`	Counts lines for all .kt files in src/main/kotlin/com/example/.
#
`./gradlew countKtLines --file=<FileName>`	Counts lines for a specific file (omit .kt extension).
#

Example:
```
./gradlew countKtLines --file=MyApp
// Output: File: MyApp.kt | Lines: 42
```

### Task 2: createFolder

Creates a folder and text file.
#
`./gradlew createFolder`	Creates plugin-folder/default.txt.
#
`./gradlew createFolder --addTxt=<name>`	Creates plugin-folder/<name>.txt.

Example:
```
./gradlew createFolder --addTxt=config
// Output: Folder: plugin-folder | File: config.txt
```
