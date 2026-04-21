package com.pethelp.app.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * Utilidad para gestionar la autenticación biométrica en la aplicación.
 *
 * Proporciona métodos para verificar la disponibilidad de biometría en el
 * dispositivo y para iniciar un flujo de autenticación con huella, rostro o
 * credenciales del dispositivo.
 */
object BiometricAuthGate {

    /**
     * Estados posibles de disponibilidad biométrica en el dispositivo.
     */
    enum class Availability {
        AVAILABLE,
        NO_HARDWARE,
        HW_UNAVAILABLE,
        NONE_ENROLLED,
        SECURITY_UPDATE_REQUIRED,
        UNSUPPORTED,
        UNKNOWN
    }

    private val authenticators: Int =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /**
     * Comprueba la disponibilidad de autenticación biométrica en el dispositivo.
     *
     * @param context contexto necesario para acceder a BiometricManager.
     * @return una constante de Availability que describe el estado.
     */
    fun availability(context: Context): Availability {
        val result = BiometricManager.from(context).canAuthenticate(authenticators)
        return when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> Availability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Availability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Availability.HW_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Availability.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> Availability.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> Availability.UNSUPPORTED
            else -> Availability.UNKNOWN
        }
    }

    /**
     * Inicia la autenticación biométrica usando el prompt del sistema.
     *
     * @param activity Activity que mostrará el prompt biometric.
     * @param executor ejecutor donde se ejecutarán los callbacks.
     * @param title título principal del prompt.
     * @param subtitle subtítulo opcional del prompt.
     * @param description descripción adicional que explique por qué se pide la autenticación.
     * @param onSuccess callback cuando la autenticación es exitosa.
     * @param onError callback cuando ocurre un error grave al autenticar.
     * @param onFailed callback cuando la biometría se intenta pero no coincide.
     */
    fun authenticate(
        activity: FragmentActivity,
        executor: Executor,
        title: String,
        subtitle: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: CharSequence) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                onFailed()
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}

