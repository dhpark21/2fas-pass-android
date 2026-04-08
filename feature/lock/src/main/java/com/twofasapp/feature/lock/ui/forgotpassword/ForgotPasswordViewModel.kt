package com.twofasapp.feature.lock.ui.forgotpassword

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.decodeUrlParam
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.ktx.readPdfAsBitmap
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.security.crypto.MasterKey
import com.twofasapp.feature.qrscan.ReadQrFromImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ForgotPasswordViewModel(
    private val securityRepository: SecurityRepository,
    private val readQrFromImage: ReadQrFromImage,
    private val strings: Strings,
) : ViewModel() {

    val uiState = MutableStateFlow(ForgotPasswordUiState())

    fun openState(state: ForgotPasswordState) {
        uiState.update { it.copy(state = state) }
    }

    fun readDecryptionKit(
        context: Context,
        fileUri: Uri,
        onVerified: (ByteArray) -> Unit,
        onError: () -> Unit,
    ) {
        openState(ForgotPasswordState.Loading)

        launchScoped {
            context.readPdfAsBitmap(fileUri)?.let { bitmap ->
                readQrFromImage.invoke(
                    bitmap = bitmap,
                )
                    .onSuccess { uriString ->
                        verifyDecryptionKit(
                            text = uriString,
                            onVerified = onVerified,
                            onError = onError,
                        )
                    }
                    .onFailure { e ->
                        openState(
                            ForgotPasswordState.Error(
                                title = strings.forgotPasswordErrorVerificationTitle,
                                msg = strings.forgotPasswordErrorVerificationSubtitle,
                            ),
                        )
                    }
            } ?: openState(
                ForgotPasswordState.Error(
                    title = "Error reading Decryption Kit",
                    msg = "Error occurred while reading Decryption Kit.",
                ),
            )
        }
    }

    fun verifyDecryptionKit(
        text: String,
        onVerified: (ByteArray) -> Unit,
        onError: () -> Unit,
    ) {
        Flog.d("Scanned: $text")

        openState(ForgotPasswordState.Loading)

        launchScoped {
            runSafely {
                val uri = text.toUri()
                val entropy = uri.getQueryParameter("entropy")!!.decodeUrlParam().decodeBase64()
                val masterKey = uri.getQueryParameter("master_key")?.decodeUrlParam()?.decodeBase64()

                if (masterKey == null) {
                    onError()

                    openState(
                        ForgotPasswordState.Error(
                            title = strings.forgotPasswordNoMasterKeyTitle,
                            msg = strings.forgotPasswordNoMasterKeySubtitle,
                        ),
                    )

                    return@launchScoped
                }

                if (entropy.contentEquals(securityRepository.getMasterKeyEntropy()).not()) {
                    onError()

                    openState(
                        ForgotPasswordState.Error(
                            title = strings.forgotPasswordErrorVerificationTitle,
                            msg = strings.forgotPasswordErrorVerificationSubtitle,
                        ),
                    )

                    return@launchScoped
                }

                securityRepository.tryDecryptEncryptionReference(
                    key = MasterKey(hashHex = masterKey.encodeHex()),
                )

                masterKey
            }
                .onSuccess { masterKey ->
                    onVerified(masterKey)
                }
                .onFailure {
                    onError()

                    openState(
                        ForgotPasswordState.Error(
                            title = strings.forgotPasswordErrorVerificationTitle,
                            msg = strings.forgotPasswordErrorVerificationSubtitle,
                        ),
                    )
                }
        }
    }
}