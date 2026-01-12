package en.entouche

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform