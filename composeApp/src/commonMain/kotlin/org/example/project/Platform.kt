package org.example.project

interface Platform{
    val name: String
    fun showGreeting(name: String)
}

expect fun getPlatform(): Platform