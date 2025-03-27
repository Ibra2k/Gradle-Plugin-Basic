package com.example

class HackNasaFile(){

    init {
        connectToWhiteHouse()
    }

    fun connectToWhiteHouse(){
        print("Connecting To White House....")
        connectToNasa()
    }

    fun connectToNasa(){
        println("Connecting to Nasa...")
        Thread.sleep(2000)
        println("Connected to Nasa")
        hackNasa()
    }

    fun hackNasa(){
        println("Initialzing Hack...")
        Thread.sleep(2000)
        println("Hacked Nasa >:)")
    }
}