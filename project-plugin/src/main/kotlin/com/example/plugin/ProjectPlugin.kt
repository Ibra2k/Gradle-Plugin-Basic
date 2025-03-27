package com.example.plugin

import CreateProjectStructure
import org.gradle.api.Plugin
import org.gradle.api.Project

class ProjectPlugin : Plugin<Project>{

    private val pluginGroup = "Custom-Plugins"

    override fun apply(project: Project) {

        project.tasks.register("createFolder", CreateProjectStructure::class.java){
            group = pluginGroup
            description = "Creates a folder named 'plugin-folder' and a file within it 'default.txt' by default"
        }

        project.tasks.register("countKtLines", CountKtFileLines::class.java){
            group = pluginGroup
            description = "Counts all files in src/main/kotlin/com/example directory by default"
        }

    }
}