/**
 * Utilidad de seguridad para gestionar autenticación biométrica.
 *
 * Abstrae BiometricPrompt y BiometricManager para proporcionar
 * una interfaz sencilla de verificación por huella, rostro o PIN
 * sobre acciones sensibles de la aplicación.
 */
package com.pethelp.app.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * Utilidad de seguridad para gestionar la autenticación biométrica y credenciales del dispositivo.
 *
 * **Responsabilidad Principal:**
 * Actuar como un "Gatekeeper" o puerta de seguridad para acciones sensibles (ej. ver llaves privadas,
 * confirmar adopciones, cambiar contraseñas). Abstrae la complejidad de `BiometricPrompt` y
 * `BiometricManager` para proporcionar una interfaz sencilla y reactiva.
 *
 * **Características de Seguridad:**
 * - **Autenticación Fuerte:** Utiliza `BIOMETRIC_STRONG`, lo que garantiza que solo se acepten
 *   sensores biométricos con baja tasa de aceptación falsa (huella, rostro 3D).
 * - **Respaldo de Dispositivo:** Permite el uso del PIN, patrón o contraseña del sistema como
 *   método de respaldo si la biometría falla o no está configurada.
 * - **Integración con Ciclo de Vida:** Diseñado para trabajar con `FragmentActivity` para asegurar
 *   que el prompt se muestre correctamente sobre la interfaz de Compose.
 *
 * **Notas para Junior Developers:**
 * - Nunca guardes los datos biométricos tú mismo; el sistema Android se encarga de la captura y validación.
 * - Este objeto solo retorna un "Éxito" o "Error" lógico.
 * - Siempre verifica la disponibilidad con [availability] antes de llamar a [authenticate].
 *
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see BiometricPrompt Documentación oficial de Android.
 */
object BiometricAuthGate {

    /**
     * Estados posibles de disponibilidad biométrica en el dispositivo.
     * Mapea los códigos de error técnicos de Android a un Enum más legible para la lógica de negocio.
     */
    enum class Availability {
        /** El dispositivo está listo para autenticar. */
        AVAILABLE,
        /** El dispositivo no cuenta con sensores biométricos. */
        NO_HARDWARE,
        /** Los sensores están ocupados o deshabilitados temporalmente. */
        HW_UNAVAILABLE,
        /** El hardware existe pero el usuario no ha registrado ninguna huella o rostro. */
        NONE_ENROLLED,
        /** Se requiere una actualización de seguridad del sistema para usar la biometría. */
        SECURITY_UPDATE_REQUIRED,
        /** El nivel de API o el hardware no soportan el tipo de autenticador solicitado. */
        UNSUPPORTED,
        /** Error no identificado. */
        UNKNOWN
    }

    /**
     * Configuración de autenticadores:
     * - `BIOMETRIC_STRONG`: Sensores de Clase 3 (Alta seguridad).
     * - `DEVICE_CREDENTIAL`: PIN/Patrón/Password del bloqueo de pantalla.
     */
    private val authenticators: Int =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /**
     * Comprueba si el dispositivo cumple con los requisitos para mostrar el prompt de seguridad.
     *
     * **Lógica:**
     * Utiliza [BiometricManager] para consultar al sistema operativo sobre el estado actual
     * del hardware y las credenciales registradas.
     *
     * @param context Contexto de la aplicación o actividad.
     * @return Una constante de [Availability] para decidir si mostrar el botón de biometría en la UI.
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
     * Dispara el diálogo del sistema (System Prompt) para solicitar la identidad del usuario.
     *
     * **Proceso de Autenticación:**
     * 1. **Configuración:** Crea un [BiometricPrompt.PromptInfo] con los textos proporcionados.
     * 2. **Instanciación:** Crea el [BiometricPrompt] vinculado a la actividad actual.
     * 3. **Callback:** Define los escuchas para éxito, error (ej. cancelación) o fallo (identidad no reconocida).
     * 4. **Ejecución:** Muestra el diálogo modal del sistema.
     *
     * @param activity La actividad actual (debe extender de [FragmentActivity]).
     * @param executor Hilo donde se procesarán los resultados (usualmente `ContextCompat.getMainExecutor(context)`).
     * @param title Título llamativo (ej. "Confirmar Identidad").
     * @param subtitle Breve explicación (ej. "Usa tu huella para continuar").
     * @param description Detalle del motivo (ej. "Necesitamos verificar que eres tú para autorizar esta adopción").
     * @param onSuccess Ejecutado cuando el sistema confirma la identidad.
     * @param onError Ejecutado ante errores fatales o cancelación del usuario.
     * @param onFailed Ejecutado cuando el sensor detecta algo que no coincide con los datos registrados.
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
        // PASO 1: Definir los manejadores de eventos.
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

        // PASO 2: Construir la información visual del diálogo.
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators) // IMPORTANTE: Define qué métodos se permiten.
            .build()

        // PASO 3: Iniciar la autenticación.
        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}

