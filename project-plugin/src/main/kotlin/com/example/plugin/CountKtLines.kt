package com.example.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File


abstract class CountKtFileLines: DefaultTask(){

    private val ktFileDir = "src/main/kotlin/com/example/"
    private var fileName: String? = null


    @get:InputDirectory
    val ktFilesDir= project.fileTree(ktFileDir)

    @Option(option = "file", description = "Counts lines of a specific kotlin file. [Do NOT Add .kt]")
    fun specificFileName(name: String?){
        fileName = name
    }


    @TaskAction
    fun countKtFiles(){
        fileName?.let { optionTask(it) } ?: defaultTask()
    }

    fun optionTask(fileName: String){
        val file = project.file("$ktFileDir/$fileName.kt")
        log(file, countLines(file))
    }

    fun defaultTask(){
        ktFilesDir.forEach { file ->
            val lineCount = countLines(file)
            log(file, lineCount)
        }
    }

    fun countLines(file: File): Int {
        var lineCount = 0
        file.forEachLine { eachLine ->
            if (eachLine.isNotBlank()) lineCount += 1
        }
        return lineCount
    }


    fun log(file: File, lineCount: Int){
        logger.quiet("File: ${file.name} | Lines: $lineCount")
    }
}

