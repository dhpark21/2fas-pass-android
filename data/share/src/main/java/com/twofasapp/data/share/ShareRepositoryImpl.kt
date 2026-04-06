package com.twofasapp.data.share

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.Pbkdf
import com.twofasapp.core.common.crypto.RandomGenerator
import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.decodeBase64UrlSafe
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.ktx.encodeBase64UrlSafe
import com.twofasapp.core.network.ApiConfig
import com.twofasapp.data.share.domain.ShareItem
import com.twofasapp.data.share.domain.ShareLink
import com.twofasapp.data.share.mapper.ShareMapper
import com.twofasapp.data.share.remote.ShareRemoteSource
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber

internal class ShareRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val apiConfig: ApiConfig,
    private val shareRemoteSource: ShareRemoteSource,
    private val shareMapper: ShareMapper,
    private val json: Json,
) : ShareRepository {

    private val decryptedShareItems = mutableMapOf<String, Item>()

    override suspend fun createShareLink(
        item: Item,
        expirationTimeSeconds: Int,
        oneTimeAccess: Boolean,
        password: String?,
    ): ShareLink {
        return withContext(dispatchers.io) {
            val isPasswordProtected = !password?.trim().isNullOrEmpty()
            val shareContent = json.encodeToString(ShareItem.serializer(), shareMapper.map(item))
            Timber.d("Sharing content: $shareContent")

            val salt = RandomGenerator.generate(bytes = 16)
            val randomKey = RandomGenerator.generate(bytes = 32)

            val encryptionKey = if (isPasswordProtected) {
                Pbkdf.generate(
                    input = password,
                    salt = salt,
                    iterations = 600_000,
                    keyLength = 256,
                )
            } else {
                randomKey
            }

            val encrypted = encrypt(
                key = encryptionKey,
                data = shareContent,
            )

            val response = shareRemoteSource.createShareLink(
                data = encrypted.data.encodeBase64(),
                expirationTimeSeconds = expirationTimeSeconds,
                oneTimeAccess = oneTimeAccess,
            )

            val uuid = response.id
            val nonce = encrypted.iv.encodeBase64UrlSafe()
            val version = if (isPasswordProtected) "v1p" else "v1k"
            val key = if (isPasswordProtected) salt.encodeBase64UrlSafe() else randomKey.encodeBase64UrlSafe()

            ShareLink(
                id = response.id,
                url = "${apiConfig.shareApiUrl}/#/$uuid/$version/$nonce/$key",
            )
        }
    }

    override suspend fun decryptShareLink(
        shareId: String,
        version: String,
        nonce: String,
        key: String,
        password: String?,
    ): Item {
        return withContext(dispatchers.io) {
            val response = shareRemoteSource.getShareSecret(shareId)
            val encryptedData = response.data.decodeBase64()
            val iv = nonce.decodeBase64UrlSafe()

            val decryptionKey = when (version) {
                "v1k" -> key.decodeBase64UrlSafe()
                "v1p" -> {
                    requireNotNull(password) { "Password is required for v1p share links" }
                    val salt = key.decodeBase64UrlSafe()
                    Pbkdf.generate(
                        input = password,
                        salt = salt,
                        iterations = 600_000,
                        keyLength = 256,
                    )
                }
                else -> throw IllegalArgumentException("Unsupported share link version: $version")
            }

            val encrypted = EncryptedBytes(iv = iv, data = encryptedData)
            val decryptedBytes = decrypt(key = decryptionKey, data = encrypted)
            val decryptedJson = decryptedBytes.toString(Charsets.UTF_8)

            Timber.d("Decrypted share content: $decryptedJson")

            val shareItem = json.decodeFromString(ShareItem.serializer(), decryptedJson)
            shareMapper.map(shareItem)
        }
    }

    override fun cacheDecryptedShareItem(shareId: String, item: Item) {
        decryptedShareItems[shareId] = item
    }

    override fun getDecryptedShareItem(shareId: String): Item? {
        return decryptedShareItems[shareId]
    }

    override fun removeDecryptedShareItem(shareId: String) {
        decryptedShareItems.remove(shareId)
    }
}