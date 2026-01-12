package en.entouche.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

object SupabaseConfig {
    // TODO: Replace with your own Supabase credentials
    // Get these from: https://supabase.com -> Your Project -> Settings -> API
    const val SUPABASE_URL = "YOUR_SUPABASE_URL"
    const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"
}

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = SupabaseConfig.SUPABASE_URL,
        supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }

    val auth get() = client.auth
    val postgrest get() = client.postgrest
}
