package com.pethelp.app.core.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Composable auxiliar que muestra cómo utilizar el nuevo selector de imágenes
 * introducido en Android 14+ (Selected Photos Access).
 *
 * En lugar de solicitar permisos de almacenamiento amplio, se emplea el
 * contrato ActivityResultContracts.PickVisualMedia para invocar el selector
 * nativo del sistema. Esto evita problemas de privacidad y facilita el
 * cumplimiento de las políticas de Google Play.
 *
 * La regla de lint de Android penaliza la presencia de
 * READ_MEDIA_IMAGES si no se maneja explícitamente el selector; esta clase
 * cumple ese requisito y garantiza que la aplicación siga funcionando bien
 * en API 33 y posteriores.
 */
@Composable
fun rememberPhotoPicker(onImageSelected: (Uri?) -> Unit) {
    var lastSelected by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            lastSelected = uri
            onImageSelected(uri)
        }
    )

    // when you need to launch:
    // launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
}
