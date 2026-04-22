# Estado de documentacion del proyecto

Fecha de auditoria: 2026-04-21
Ultima actualizacion: 2026-04-21 (inicio de ejecucion)

## Cobertura general (codigo Kotlin)
- Total archivos Kotlin (`app/src/main/java`): **71**
- Archivos con KDoc: **71**
- Archivos sin KDoc: **0**

## Prioridad por modulo
- `feature:profile`: 0 sin KDoc
- `feature:post`: 0 sin KDoc
- `core`: 0 sin KDoc

> Nota: hay un posible desfase de conteo por cambios recientes; se recomienda revalidar al terminar cada bloque.

## Archivos Kotlin sin KDoc detectados

- Ninguno. Cobertura completa en `app/src/main/java`.

## Hallazgos de documentacion funcional
- El `README.md` no enlaza:
  - `Docs/GuiaVistas.md`
  - `Docs/PromptsFigma.md`

## Avance aplicado
- Documentado: `app/src/main/java/com/pethelp/app/features/post/domain/model/AdoptionRequest.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/data/repository/FirebasePostRepository.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/AdoptionRequestsViewModel.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/AdoptionRequestViewModel.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/EditPostViewModel.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/FavoritesViewModel.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/MyPostsViewModel.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/PostDetailViewModel.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/FavoritesScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/LocationSelectionScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/MyPostsScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/PostDetailsScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/post/presentation/PostReviewScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/di/ProfileModule.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/domain/repository/ProfileRepository.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/ProfileUiState.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/ProfileViewModel.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/ProfileScreens.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/SettingsScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/HelpCenterScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/LanguageScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/PrivacyScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/ProfileVisibilityScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/SecurityScreen.kt`
- Documentado: `app/src/main/java/com/pethelp/app/features/profile/presentation/UserGuideScreen.kt`

## Recomendacion de inicio
1. Empezar por `feature:post`:
   - Primero contratos/modelo y repositorio (`AdoptionRequest.kt`, `FirebasePostRepository.kt`).
   - Luego `ViewModel`.
   - Por ultimo pantallas (`*Screen.kt`).
2. `feature:post` y `feature:profile` cerrados.
