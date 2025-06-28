package org.example.project

interface Platform{
    val name: String
    fun showGreeting(name: String)
    fun saveTextToFile(content: String, defaultFileName: String)
    fun stringFormat(format : String, vararg args: Int): String
}

expect fun getPlatform(): Platform