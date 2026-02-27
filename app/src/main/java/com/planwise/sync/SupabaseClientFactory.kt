package com.planwise.sync

import com.planwise.data.settings.SupabaseSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientFactory {
    fun create(url: String, key: String): SupabaseClient =
        createSupabaseClient(url, key) {
            install(Auth)
            install(Postgrest)
        }

    fun toUserSession(session: SupabaseSession): UserSession =
        UserSession(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            expiresIn = session.expiresIn,
            tokenType = session.tokenType,
            user = null,
        )
}
