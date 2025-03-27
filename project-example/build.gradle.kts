plugins {
    kotlin("jvm") version "2.0.21"
    application
    id("com.example.make-project") version "1.0.0"
}

repositories {
    mavenCentral()
}


application {
    mainClass.set("com.example.MainKt")
}