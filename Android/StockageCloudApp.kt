package com.stockagecloud.app

import android.app.Application
import com.stockagecloud.app.data.local.TokenDataStore
import com.stockagecloud.app.data.remote.ApiService
import com.stockagecloud.app.data.remote.RetrofitClient
import com.stockagecloud.app.data.repository.AuthRepository
import com.stockagecloud.app.data.repository.FichierRepository

class StockageCloudApp : Application() {

    lateinit var tokenDataStore: TokenDataStore
        private set
    lateinit var apiService: ApiService
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var fichierRepository: FichierRepository
        private set

    override fun onCreate() {
        super.onCreate()
        tokenDataStore = TokenDataStore(this)
        apiService = RetrofitClient.creer(tokenDataStore)
        authRepository = AuthRepository(apiService, tokenDataStore)
        fichierRepository = FichierRepository(apiService)
    }
}
