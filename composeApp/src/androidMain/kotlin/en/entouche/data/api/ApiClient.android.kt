package en.entouche.data.api

/**
 * Android emulator uses 10.0.2.2 to access host machine's localhost
 */
actual fun getBaseUrl(): String = "http://10.0.2.2:8080"
