import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class CreateProjectStructure: DefaultTask() {

    private var txtFileName: String? = null
    private val folderName = "plugin-folder"

    @Option(option = "addTxt", description = "Creates a custom txt file [eg. --addTxt=newFile]")
    fun addTxt(name:String?){
        txtFileName = name
    }

    @TaskAction
    fun createFolder(){
        project.mkdir(folderName)
        project.file("$folderName/${txtFileName?.let { "$it.txt" } ?: "default.txt"}").writeText("Test File")
        logger.quiet("Folder: $folderName | File: $txtFileName")
    }
}

