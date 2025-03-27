plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
} 

repositories {
	mavenCentral()
}

gradlePlugin {
	plugins { 
		create("createProjectStructure"){
			id="com.example.make-project"
			implementationClass="com.example.plugin.ProjectPlugin"
		}
	}
}

