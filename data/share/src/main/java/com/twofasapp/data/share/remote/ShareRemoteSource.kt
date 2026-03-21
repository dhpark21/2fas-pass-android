package com.twofasapp.data.share.remote

import com.twofasapp.core.network.ApiConfig
import com.twofasapp.data.share.remote.model.ShareItemJson
import com.twofasapp.data.share.remote.model.ShareLinkJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

internal class ShareRemoteSource(
    private val apiConfig: ApiConfig,
    private val httpClient: HttpClient,
) {
    suspend fun createShareLink(
        data: String,
        expirationTimeSeconds: Int,
        oneTimeAccess: Boolean,
    ): ShareLinkJson {
        return httpClient.post("${apiConfig.shareApiUrl}/secret") {
            setBody(
                ShareItemJson(
                    data = data,
                    validForSeconds = expirationTimeSeconds,
                    singleUse = oneTimeAccess,
                ),
            )
        }.body()
    }
}