package emy.partners.lawapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform