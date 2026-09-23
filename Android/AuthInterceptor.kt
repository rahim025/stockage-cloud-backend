package com.stockagecloud.app.data.remote

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import com.stockagecloud.app.data.local.TokenDataStore

class AuthInterceptor(private val tokenDataStore: TokenDataStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenDataStore.token.first() }

        val requeteOriginale = chain.request()

        // Les routes d'auth n'ont pas besoin du header ; les autres oui, si le token existe.
        val requete = if (token != null) {
            requeteOriginale.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            requeteOriginale
        }

        return chain.proceed(requete)
    }
}
