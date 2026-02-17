# Épicas e Historias de Usuario — PetHelp

> **Método:** cada historia de usuario cumple con los criterios **INVEST** (Independent, Negotiable, Valuable, Estimable, Small, Testable).  
> **Formato:** _Como [rol], quiero [acción], para [beneficio]._

---

## Mapa de trazabilidad Enunciado → Épicas

| Requisito del enunciado | Épica |
|---|---|
| Registro, login, recuperación de contraseña, editar perfil, eliminar cuenta | EP-01 |
| CRUD de publicaciones, campos de mascota, estados | EP-02 |
| Feed lista/mapa, filtros, detalle de publicación | EP-03 |
| Moderador: aprobar/rechazar, marcar resuelta, verificar veracidad | EP-04 |
| Comentarios, votos "Me interesa", solicitud "Me interesa adoptar" | EP-05 |
| FCM, notificaciones georreferenciadas, lista de notificaciones | EP-06 |
| Mapa para ubicación, visualización en mapa, filtro por cercanía | EP-07 |
| Subida de imágenes a servicio externo | EP-08 |
| Dashboard de estadísticas personales | EP-09 |
| Puntos, niveles, insignias/badges | EP-10 |
| Funcionalidad con LLM (elegir 1 de 5) | EP-11 |
| Soporte multi-idioma (Fase 3) | EP-12 |
| Necesidad adicional no trivial | EP-13 |

---

## EP-01 — Autenticación y Gestión de Cuentas

**Objetivo:** permitir a usuarios y moderadores acceder de forma segura a la aplicación, gestionar sus credenciales y administrar su perfil.

### HU-01.1 — Registro de usuario
**Como** usuario nuevo, **quiero** crear una cuenta con mi correo electrónico y una contraseña, **para** acceder a las funcionalidades de la aplicación.

**Criterios de aceptación:**
1. El formulario solicita: nombre, correo electrónico y contraseña.
2. El sistema valida formato de correo y fortaleza de contraseña.
3. No se permite registrar un correo ya existente; se muestra mensaje de error.
4. Al completar el registro, el usuario es redirigido a la pantalla de inicio (feed).
5. La cuenta se almacena en el sistema de autenticación (Firebase Auth u otro).

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | No depende de otras HU para implementarse. |
| Negotiable | Los campos adicionales del perfil (foto, dirección) pueden definirse después. |
| Valuable | Sin registro no se puede usar la app; es requisito habilitante. |
| Estimable | Formulario + validación + integración con Auth; estimable en 3-5 días. |
| Small | Una sola pantalla con lógica de validación y persistencia. |
| Testable | Se verifica con pruebas de registro exitoso, correo duplicado y validaciones. |

---

### HU-01.2 — Inicio de sesión (Usuario)
**Como** usuario registrado, **quiero** iniciar sesión con mi correo y contraseña, **para** acceder a mi cuenta y a mis publicaciones.

**Criterios de aceptación:**
1. El formulario solicita correo electrónico y contraseña.
2. Credenciales válidas redirigen al feed principal.
3. Credenciales inválidas muestran mensaje de error claro.
4. La sesión persiste hasta que el usuario cierre sesión manualmente.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Depende solo de que exista la HU-01.1 (registro). |
| Negotiable | Opciones como "recordarme" o biometría pueden añadirse después. |
| Valuable | Permite acceder a la app; requisito fundamental. |
| Estimable | Formulario + llamada Auth; estimable en 2-3 días. |
| Small | Una pantalla con lógica de autenticación. |
| Testable | Login exitoso, login fallido, persistencia de sesión. |

---

### HU-01.3 — Inicio de sesión (Moderador)
**Como** moderador precargado, **quiero** iniciar sesión con mis credenciales, **para** acceder al panel de moderación.

**Criterios de aceptación:**
1. El moderador usa el mismo formulario de login que el usuario.
2. Al autenticarse, el sistema detecta el rol "Moderador" y redirige al panel de moderación.
3. Los moderadores están precargados en la base de datos; no existe registro público de moderadores.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Comparte pantalla con HU-01.2 pero la lógica de rol es independiente. |
| Negotiable | El panel al que redirige puede diseñarse después. |
| Valuable | Habilita toda la funcionalidad de moderación. |
| Estimable | Detección de rol + redirección; estimable en 1-2 días. |
| Small | Lógica condicional sobre el login existente. |
| Testable | Login con cuenta moderador → panel de moderación; login con cuenta usuario → feed. |

---

### HU-01.4 — Recuperación de contraseña
**Como** usuario o moderador, **quiero** recuperar mi contraseña mediante un enlace enviado a mi correo, **para** volver a acceder a mi cuenta si olvido la contraseña.

**Criterios de aceptación:**
1. Existe un botón "¿Olvidaste tu contraseña?" en la pantalla de login.
2. Se solicita el correo electrónico registrado.
3. Se envía un enlace de restablecimiento al correo.
4. El enlace permite definir una nueva contraseña.
5. Si el correo no está registrado, se muestra un mensaje informativo.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente del flujo de login normal. |
| Negotiable | El diseño del email y la pantalla de nueva contraseña son flexibles. |
| Valuable | Evita la pérdida permanente de acceso. |
| Estimable | Integración con Firebase Auth `sendPasswordResetEmail`; 2-3 días. |
| Small | Un formulario + integración con servicio de correo. |
| Testable | Se prueba envío, recepción del enlace y cambio de contraseña. |

---

### HU-01.5 — Editar datos personales
**Como** usuario, **quiero** editar mis datos personales (nombre, foto, etc.), **para** mantener mi perfil actualizado.

**Criterios de aceptación:**
1. Existe una pantalla de perfil accesible desde el menú/navegación.
2. El usuario puede modificar su nombre y otros datos editables.
3. Los cambios se persisten y se reflejan inmediatamente.
4. Se muestra confirmación visual al guardar exitosamente.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de otras HU (solo requiere sesión activa). |
| Negotiable | Los campos editables pueden definirse en diseño. |
| Valuable | Permite personalización y corrección de datos. |
| Estimable | Formulario de edición + update en BD; 2-3 días. |
| Small | Una pantalla de edición con persistencia. |
| Testable | Editar nombre → verificar que el cambio persiste. |

---

### HU-01.6 — Eliminar cuenta
**Como** usuario, **quiero** eliminar mi cuenta permanentemente, **para** retirar mis datos de la plataforma.

**Criterios de aceptación:**
1. Existe opción de eliminar cuenta en la configuración del perfil.
2. Se solicita confirmación antes de proceder (diálogo de confirmación).
3. Al confirmar, se eliminan los datos del usuario del sistema de autenticación y de la base de datos.
4. El usuario es redirigido a la pantalla de login/registro.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente; solo requiere sesión activa. |
| Negotiable | La política sobre publicaciones huérfanas puede negociarse. |
| Valuable | Cumple con el derecho del usuario a retirar sus datos. |
| Estimable | Confirmación + delete en Auth + delete en BD; 2-3 días. |
| Small | Un diálogo + lógica de eliminación. |
| Testable | Eliminar cuenta → no se puede re-loguear; datos borrados de BD. |

---

## EP-02 — Gestión de Publicaciones de Mascotas

**Objetivo:** permitir al usuario crear, editar, eliminar y gestionar el ciclo de vida completo de publicaciones de mascotas con todos los campos requeridos.

### HU-02.1 — Crear publicación de mascota
**Como** usuario, **quiero** crear una publicación con los datos de una mascota (título, categoría, descripción, tipo de animal, raza aproximada, tamaño, estado de vacunación, ubicación e imágenes), **para** dar a conocer una mascota en adopción, perdida, encontrada, en hogar temporal o un evento veterinario.

**Criterios de aceptación:**
1. El formulario incluye: título, categoría (Adopción / Perdidos / Encontrados / Hogar temporal / Eventos veterinarios), descripción, tipo de animal, raza aproximada, tamaño, estado de vacunación.
2. Se debe seleccionar una ubicación en el mapa (lat/long).
3. Se debe adjuntar al menos una imagen.
4. La publicación se crea con estado **"Pendiente de verificación"** (no aparece en el feed público hasta que un moderador la apruebe).
5. Se muestra confirmación al usuario tras la creación exitosa.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Puede desarrollarse con datos en memoria antes de integrar mapa/imágenes. |
| Negotiable | El orden de los campos y la UX del formulario son flexibles. |
| Valuable | Es la funcionalidad central de la aplicación. |
| Estimable | Formulario multi-paso + validaciones; 4-6 días. |
| Small | Una pantalla/flujo de creación con validaciones. |
| Testable | Crear publicación → verificar que aparece con estado pendiente y todos los campos. |

---

### HU-02.2 — Editar publicación propia
**Como** usuario, **quiero** editar una publicación que he creado, **para** corregir o actualizar la información de la mascota.

**Criterios de aceptación:**
1. Solo el autor puede editar su publicación.
2. Todos los campos son editables (título, descripción, categoría, datos de mascota, ubicación, imágenes).
3. Los cambios se persisten y se reflejan en el detalle y en el feed.
4. Si la publicación ya estaba verificada, al editarla vuelve a estado "Pendiente de verificación".

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Requiere que exista la publicación (HU-02.1), pero la lógica de edición es independiente. |
| Negotiable | Qué sucede con el estado tras editar puede acordarse. |
| Valuable | Permite mantener información precisa y actualizada. |
| Estimable | Reutiliza el formulario de creación + lógica de update; 2-3 días. |
| Small | Carga datos existentes en formulario + persistencia de cambios. |
| Testable | Editar campo → verificar que el cambio persiste; verificar cambio de estado. |

---

### HU-02.3 — Eliminar publicación propia
**Como** usuario, **quiero** eliminar una publicación que he creado, **para** retirar información que ya no es relevante.

**Criterios de aceptación:**
1. Solo el autor puede eliminar su publicación.
2. Se solicita confirmación antes de eliminar.
3. La publicación y sus datos asociados (comentarios, votos) se eliminan de la base de datos.
4. La publicación desaparece del feed y del perfil del usuario.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente en lógica; solo requiere publicación existente. |
| Negotiable | Puede ser borrado lógico o físico; negociable. |
| Valuable | Da control al usuario sobre su contenido. |
| Estimable | Diálogo de confirmación + delete; 1-2 días. |
| Small | Una acción con confirmación y persistencia. |
| Testable | Eliminar → publicación no aparece en feed ni en perfil. |

---

### HU-02.4 — Cambiar estado a Resuelta/Finalizada (por usuario)
**Como** usuario autor de una publicación, **quiero** marcar mi publicación como "Resuelta/Finalizada", **para** indicar que la situación de la mascota ya se resolvió (fue adoptada, encontrada, etc.).

**Criterios de aceptación:**
1. Solo el autor puede cambiar el estado a "Resuelta/Finalizada".
2. Se muestra un botón o acción visible en el detalle de la publicación.
3. La publicación marcada como resuelta se distingue visualmente en el feed.
4. El cambio de estado se persiste en la base de datos.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente; solo requiere publicación existente. |
| Negotiable | Si puede revertirse el estado es negociable. |
| Valuable | Evita interacciones innecesarias y mantiene el feed limpio. |
| Estimable | Un cambio de estado + UI; 1-2 días. |
| Small | Un botón + update de estado. |
| Testable | Marcar como resuelta → verificar estado y visualización diferenciada. |

---

## EP-03 — Feed y Exploración

**Objetivo:** permitir a los usuarios descubrir y explorar publicaciones de mascotas de forma eficiente, por medio de vistas en lista y mapa, con filtros de categoría y ubicación.

### HU-03.1 — Ver feed en modo lista
**Como** usuario, **quiero** ver un feed de publicaciones verificadas en forma de lista, **para** explorar rápidamente las mascotas disponibles.

**Criterios de aceptación:**
1. La pantalla de inicio muestra las publicaciones verificadas en formato de lista scrollable.
2. Cada ítem del listado muestra: imagen principal, título, categoría, ubicación resumida y cantidad de votos.
3. Solo se muestran publicaciones con estado "Verificada" (no pendientes ni rechazadas).
4. Se puede hacer tap en un ítem para ir al detalle (HU-03.4).

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Puede desarrollarse con datos mock antes de integrar BD. |
| Negotiable | Diseño de la tarjeta y campos visibles son flexibles. |
| Valuable | Es la pantalla principal; punto de entrada a la app. |
| Estimable | Lista + card component + datos; 3-4 días. |
| Small | Una pantalla con lista de tarjetas. |
| Testable | Verificar que solo muestra publicaciones verificadas; verificar navegación al detalle. |

---

### HU-03.2 — Ver feed en modo mapa
**Como** usuario, **quiero** ver las publicaciones verificadas ubicadas en un mapa, **para** identificar mascotas cercanas a mi ubicación geográfica.

**Criterios de aceptación:**
1. Existe un toggle o tab para cambiar entre vista lista y vista mapa.
2. El mapa muestra marcadores en las coordenadas de cada publicación verificada.
3. Al tocar un marcador, se muestra un resumen (título, categoría, imagen) con opción de ir al detalle.
4. El mapa se centra inicialmente en la ubicación del usuario (si otorga permiso).

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de la vista lista; comparten datos pero son vistas distintas. |
| Negotiable | Proveedor de mapas (Google Maps / Mapbox) negociable. |
| Valuable | Permite descubrir mascotas por proximidad geográfica. |
| Estimable | Integración de mapa + marcadores + popup; 4-5 días. |
| Small | Una vista de mapa con marcadores e info-windows. |
| Testable | Verificar marcadores en coordenadas correctas; verificar navegación al detalle. |

---

### HU-03.3 — Filtrar publicaciones por categoría y ubicación
**Como** usuario, **quiero** filtrar las publicaciones por categoría (Adopción, Perdidos, Encontrados, Hogar temporal, Eventos veterinarios) y por ubicación (cercanos, en la ciudad), **para** encontrar rápidamente lo que me interesa.

**Criterios de aceptación:**
1. Existen controles de filtro accesibles en la pantalla del feed (chips, dropdown, etc.).
2. Filtro por categoría: selección múltiple o individual de las 5 categorías.
3. Filtro por ubicación: opciones como "Cercanos" (radio configurable en km), "En mi ciudad", "Todos".
4. Los filtros se aplican tanto en vista lista como en vista mapa.
5. Si no hay resultados, se muestra un mensaje indicativo.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Lógica de filtrado independiente de la presentación (lista/mapa). |
| Negotiable | Los rangos de km y las opciones de ubicación son negociables. |
| Valuable | Reduce ruido y mejora la experiencia de búsqueda. |
| Estimable | Componentes de filtro + lógica de query; 3-4 días. |
| Small | Componentes UI + lógica de filtrado. |
| Testable | Aplicar filtro por categoría → solo esa categoría visible; filtro por ubicación → solo cercanos. |

---

### HU-03.4 — Ver detalle de publicación
**Como** usuario, **quiero** ver el detalle completo de una publicación (título, descripción, categoría, tipo de animal, raza, tamaño, vacunación, fotos, ubicación exacta en mapa, comentarios), **para** obtener toda la información sobre la mascota.

**Criterios de aceptación:**
1. Se muestra toda la información de la publicación: título, descripción, categoría, tipo de animal, raza aproximada, tamaño, estado de vacunación.
2. Se muestran todas las imágenes adjuntas (carrusel o galería).
3. Se muestra la ubicación en un mini-mapa o enlace a mapa.
4. Se muestra la lista de comentarios con autor y fecha.
5. Se muestra el conteo de votos/interesados.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Depende de que existan publicaciones, pero la pantalla es independiente. |
| Negotiable | Layout y disposición de secciones es negociable. |
| Valuable | Información completa para tomar decisiones (adoptar, contactar, etc.). |
| Estimable | Pantalla de detalle con secciones; 3-4 días. |
| Small | Una pantalla con secciones de datos, imágenes, mapa y comentarios. |
| Testable | Abrir detalle → todos los campos presentes y correctos; comentarios visibles. |

---

## EP-04 — Moderación de Contenido

**Objetivo:** permitir a los moderadores revisar, verificar o rechazar publicaciones, y asegurar la calidad y veracidad del contenido en la plataforma.

### HU-04.1 — Ver panel de publicaciones pendientes
**Como** moderador, **quiero** ver una lista de publicaciones pendientes de verificación, **para** revisarlas y decidir si se aprueban o rechazan.

**Criterios de aceptación:**
1. El panel de moderación muestra todas las publicaciones con estado "Pendiente de verificación".
2. Cada ítem muestra: título, categoría, autor, fecha de creación e imagen principal.
3. Se puede acceder al detalle completo de cada publicación desde el panel.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente; solo consulta publicaciones pendientes. |
| Negotiable | Diseño del panel y orden de prioridad son flexibles. |
| Valuable | Da visibilidad al moderador sobre el trabajo pendiente. |
| Estimable | Lista filtrada por estado; 2-3 días. |
| Small | Una pantalla de listado filtrado. |
| Testable | Solo publicaciones pendientes visibles; acceso al detalle funciona. |

---

### HU-04.2 — Aprobar publicación
**Como** moderador, **quiero** aprobar una publicación pendiente, **para** que sea visible en el feed público.

**Criterios de aceptación:**
1. Existe un botón "Aprobar" en el detalle de la publicación (vista de moderador).
2. Al aprobar, el estado cambia a "Verificada".
3. La publicación aparece en el feed público (lista y mapa).
4. Se notifica al autor que su publicación fue aprobada.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de la acción de rechazar. |
| Negotiable | Si se envía notificación al autor es negociable. |
| Valuable | Habilita la publicación para el público. |
| Estimable | Cambio de estado + notificación opcional; 1-2 días. |
| Small | Un botón + update de estado. |
| Testable | Aprobar → publicación visible en feed; estado = "Verificada". |

---

### HU-04.3 — Rechazar publicación con motivo
**Como** moderador, **quiero** rechazar una publicación y registrar un motivo de rechazo, **para** que el autor entienda por qué fue rechazada y la publicación no aparezca en el feed público.

**Criterios de aceptación:**
1. Existe un botón "Rechazar" en el detalle de la publicación (vista de moderador).
2. Al rechazar, se abre un campo de texto obligatorio para escribir el motivo.
3. El estado cambia a "Rechazada"; la publicación no se muestra en el feed público.
4. El autor puede ver el motivo de rechazo en su lista de publicaciones.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de la acción de aprobar. |
| Negotiable | El formato del motivo y si se permite re-enviar son negociables. |
| Valuable | Mantiene la calidad del contenido y da feedback al autor. |
| Estimable | Campo de texto + cambio de estado + almacenamiento del motivo; 2-3 días. |
| Small | Un diálogo/formulario + update de estado. |
| Testable | Rechazar sin motivo → error; rechazar con motivo → estado "Rechazada" + motivo visible al autor. |

---

### HU-04.4 — Marcar publicación como resuelta/finalizada (por moderador)
**Como** moderador, **quiero** marcar cualquier publicación como resuelta/finalizada, **para** indicar que la situación ha sido atendida.

**Criterios de aceptación:**
1. El moderador puede marcar como resuelta cualquier publicación verificada.
2. El estado cambia a "Resuelta/Finalizada".
3. La publicación se distingue visualmente en el feed.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de aprobar/rechazar. |
| Negotiable | Si se notifica al autor es negociable. |
| Valuable | Permite al moderador cerrar casos que el autor no cerró. |
| Estimable | Cambio de estado; 1 día. |
| Small | Un botón + update. |
| Testable | Marcar como resuelta → estado actualizado; visualización diferenciada. |

---

## EP-05 — Interacción Social y Adopción

**Objetivo:** fomentar la participación de la comunidad mediante comentarios, votos de interés y solicitudes de adopción directas.

### HU-05.1 — Comentar en una publicación
**Como** usuario, **quiero** agregar un comentario en una publicación, **para** aportar información adicional o expresar interés.

**Criterios de aceptación:**
1. En la pantalla de detalle existe un campo para escribir y enviar un comentario.
2. El comentario se muestra en la lista de comentarios con autor, texto y fecha.
3. Al publicar un comentario, se envía una notificación al autor de la publicación.
4. El comentario se persiste en la base de datos.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de votos y adopción. |
| Negotiable | Edición/borrado de comentarios propios puede agregarse después. |
| Valuable | Permite comunicación y aporte de información entre usuarios. |
| Estimable | Input + lista + persistencia + notificación; 3-4 días. |
| Small | Un componente de input + lista de comentarios. |
| Testable | Publicar comentario → aparece en la lista; autor recibe notificación. |

---

### HU-05.2 — Votar "Me interesa" en una publicación
**Como** usuario, **quiero** marcar una publicación con "Me interesa" / votar, **para** destacar publicaciones relevantes y contribuir a determinar su relevancia.

**Criterios de aceptación:**
1. Existe un botón "Me interesa" en cada publicación (feed y detalle).
2. Cada usuario puede votar una sola vez por publicación.
3. Se puede deshacer el voto.
4. El conteo de votos se muestra actualizado en la publicación.
5. El voto suma puntos de reputación al autor de la publicación.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de comentarios y solicitud de adopción. |
| Negotiable | Efecto visual del botón y si se puede deshacer es negociable. |
| Valuable | Permite a la comunidad priorizar publicaciones importantes. |
| Estimable | Botón toggle + conteo + persistencia; 2-3 días. |
| Small | Un componente de botón + lógica de conteo. |
| Testable | Votar → conteo +1; volver a votar → conteo -1; un solo voto por usuario. |

---

### HU-05.3 — Solicitar adopción ("Me interesa adoptar")
**Como** usuario, **quiero** pulsar el botón "Me interesa adoptar" en una publicación de la categoría Adopción, **para** que el autor reciba mi solicitud y pueda contactarme.

**Criterios de aceptación:**
1. El botón "Me interesa adoptar" aparece solo en publicaciones de categoría "Adopción".
2. Al pulsar, se registra la solicitud y se notifica al autor.
3. El autor puede ver la lista de usuarios interesados en adoptar.
4. Un usuario solo puede enviar una solicitud por publicación.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de los votos genéricos y comentarios. |
| Negotiable | El flujo de aceptación/rechazo de la solicitud puede extenderse después. |
| Valuable | Funcionalidad clave de la temática; conecta adoptantes con dueños. |
| Estimable | Botón + registro de solicitud + notificación; 3-4 días. |
| Small | Un botón + lógica de solicitud + notificación. |
| Testable | Solicitar → autor ve solicitud; no se puede solicitar dos veces. |

---

## EP-06 — Notificaciones en Tiempo Real

**Objetivo:** mantener informados a los usuarios sobre eventos relevantes (nuevas publicaciones cercanas, comentarios, moderación) mediante notificaciones push georreferenciadas.

### HU-06.1 — Recibir notificaciones push de publicaciones nuevas en la zona
**Como** usuario, **quiero** recibir notificaciones push cuando se publiquen nuevas mascotas en mi zona, **para** enterarme oportunamente de mascotas cercanas.

**Criterios de aceptación:**
1. Se utiliza Firebase Cloud Messaging (u otro servicio) para enviar notificaciones push.
2. Las notificaciones son georreferenciadas: solo se envían si la publicación está dentro del radio (km) configurado respecto a la ubicación del usuario.
3. La notificación muestra título y categoría de la publicación.
4. Al tocar la notificación, se abre el detalle de la publicación.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente del feed y de los comentarios. |
| Negotiable | El radio por defecto y si es configurable son negociables. |
| Valuable | Asegura que el usuario no pierda publicaciones relevantes en su zona. |
| Estimable | FCM + lógica de geofiltrado + payload; 4-5 días. |
| Small | Integración FCM + filtro por distancia. |
| Testable | Publicación nueva dentro del radio → notificación recibida; fuera del radio → no se recibe. |

---

### HU-06.2 — Recibir notificación al recibir un comentario
**Como** autor de una publicación, **quiero** recibir una notificación cuando alguien comente en mi publicación, **para** responder o revisar la información aportada.

**Criterios de aceptación:**
1. Al registrarse un nuevo comentario, se envía notificación push al autor de la publicación.
2. La notificación indica quién comentó y en cuál publicación.
3. Al tocar la notificación, se navega al detalle de la publicación.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de notificaciones georreferenciadas. |
| Negotiable | Formato del mensaje y agrupación de notificaciones negociables. |
| Valuable | Permite al autor reaccionar rápidamente a nueva información. |
| Estimable | Trigger al comentar + envío FCM; 2-3 días. |
| Small | Un trigger + un envío de notificación. |
| Testable | Comentar → autor recibe notificación; tocar → navega al detalle. |

---

### HU-06.3 — Ver lista de notificaciones
**Como** usuario, **quiero** acceder a un apartado con la lista de todas mis notificaciones, **para** revisar eventos pasados que no haya visto.

**Criterios de aceptación:**
1. Existe una pantalla/sección de notificaciones accesible desde la navegación principal.
2. Las notificaciones se muestran en orden cronológico (más recientes primero).
3. Cada notificación muestra: tipo de evento, resumen y fecha.
4. Tocar una notificación navega al recurso correspondiente (publicación, comentario).
5. Se distinguen notificaciones leídas de no leídas.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente del envío de notificaciones; es una vista de consulta. |
| Negotiable | Diseño, agrupación y paginación son negociables. |
| Valuable | Permite al usuario no perder información importante. |
| Estimable | Lista + modelo de notificación + estados; 3-4 días. |
| Small | Una pantalla de listado con estados. |
| Testable | Notificaciones aparecen en la lista; marcado leído/no leído; navegación al recurso. |

---

## EP-07 — Mapas y Geolocalización

**Objetivo:** integrar servicios de mapas para seleccionar ubicaciones al crear publicaciones y visualizar publicaciones en el mapa.

### HU-07.1 — Seleccionar ubicación en mapa al crear/editar publicación
**Como** usuario, **quiero** seleccionar la ubicación de la mascota en un mapa interactivo al crear o editar una publicación, **para** registrar la latitud y longitud exactas.

**Criterios de aceptación:**
1. En el formulario de creación/edición se integra un mapa interactivo.
2. El usuario puede navegar el mapa y colocar un marcador en la ubicación deseada.
3. Las coordenadas (latitud, longitud) del marcador se almacenan en la publicación.
4. Opcionalmente, se muestra una dirección textual a partir de las coordenadas (geocodificación inversa).

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de la visualización del feed en mapa. |
| Negotiable | Proveedor de mapa (Google Maps / Mapbox) y geocodificación inversa negociables. |
| Valuable | Ubicación precisa es requisito obligatorio del enunciado. |
| Estimable | Integración de SDK de mapa + picker de ubicación; 3-5 días. |
| Small | Un componente de mapa con selección de punto. |
| Testable | Seleccionar punto → coordenadas almacenadas correctamente. |

---

### HU-07.2 — Obtener ubicación del usuario
**Como** usuario, **quiero** que la app obtenga mi ubicación actual (con mi permiso), **para** centrar el mapa y filtrar publicaciones cercanas.

**Criterios de aceptación:**
1. Se solicitan permisos de ubicación al usuario en tiempo de ejecución.
2. Si se otorga permiso, el mapa se centra en la ubicación actual.
3. Si se deniega, se permite navegación manual del mapa.
4. La ubicación del usuario se usa para el filtro de cercanía y para notificaciones georreferenciadas.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente; provee dato base para filtros y notificaciones. |
| Negotiable | Comportamiento al denegar permiso es negociable. |
| Valuable | Habilita filtro por cercanía y notificaciones geográficas. |
| Estimable | Solicitud de permisos + lectura de GPS; 2-3 días. |
| Small | Lógica de permisos + lectura de ubicación. |
| Testable | Permiso otorgado → mapa centrado en posición actual; denegado → navegación manual. |

---

## EP-08 — Gestión de Imágenes

**Objetivo:** permitir la subida, almacenamiento y visualización de imágenes de mascotas utilizando un servicio externo.

### HU-08.1 — Subir imágenes al crear/editar publicación
**Como** usuario, **quiero** subir imágenes de la mascota desde mi galería o cámara, **para** acompañar la publicación con evidencia visual.

**Criterios de aceptación:**
1. El formulario permite seleccionar imágenes desde la galería o capturar con la cámara.
2. Se debe subir al menos una imagen (obligatorio).
3. Las imágenes se cargan a un servicio externo (Cloudinary, AWS S3, Google Cloud Storage, etc.).
4. Se almacenan las URLs de las imágenes en la base de datos asociadas a la publicación.
5. Se muestra indicador de progreso durante la subida.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente del formulario de texto; se integra como componente. |
| Negotiable | Servicio externo, compresión y límite de imágenes son negociable. |
| Valuable | Imágenes son obligatorias y esenciales para identificar mascotas. |
| Estimable | Selector + upload + almacenamiento de URL; 3-5 días. |
| Small | Un componente de carga + integración con servicio. |
| Testable | Subir imagen → URL almacenada; sin imagen → no se puede crear publicación. |

---

### HU-08.2 — Visualizar imágenes en detalle de publicación
**Como** usuario, **quiero** ver las imágenes de una publicación en un carrusel o galería, **para** examinar la mascota visualmente.

**Criterios de aceptación:**
1. En la pantalla de detalle se muestran todas las imágenes de la publicación.
2. Se puede navegar entre imágenes (swipe o indicadores).
3. Se puede ampliar una imagen al tocarla (zoom/fullscreen).

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de la subida; solo consume URLs. |
| Negotiable | Tipo de galería (carrusel, grid) y zoom son negociables. |
| Valuable | Permite evaluación visual de la mascota. |
| Estimable | Componente de carrusel + carga de imágenes; 2-3 días. |
| Small | Un componente de galería. |
| Testable | Imágenes se cargan correctamente; se puede navegar entre ellas. |

---

## EP-09 — Estadísticas y Dashboard Personal

**Objetivo:** proporcionar al usuario un resumen visual de su actividad en la plataforma.

### HU-09.1 — Ver dashboard de estadísticas personales
**Como** usuario, **quiero** consultar un dashboard con mis métricas personales (publicaciones activas, finalizadas, pendientes de verificación), **para** tener visibilidad sobre mi actividad en la plataforma.

**Criterios de aceptación:**
1. Existe una pantalla de estadísticas accesible desde el perfil o la navegación.
2. Se muestran al menos: cantidad de publicaciones activas, finalizadas y pendientes de verificación.
3. Las métricas se actualizan en tiempo real o al abrir la pantalla.
4. Se presenta la información de forma visual (números, gráficos, iconos).

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente; consulta datos agregados del usuario. |
| Negotiable | Tipo de gráficos y métricas adicionales son negociables. |
| Valuable | Da al usuario control y visibilidad sobre su participación. |
| Estimable | Consultas agregadas + UI de dashboard; 3-4 días. |
| Small | Una pantalla con conteos y visualizaciones. |
| Testable | Crear publicaciones → las métricas reflejan los conteos correctos. |

---

## EP-10 — Sistema de Reputación y Logros

**Objetivo:** incentivar la participación activa mediante puntos, niveles y badges que reconozcan las contribuciones de cada usuario.

### HU-10.1 — Acumular puntos por participación
**Como** usuario, **quiero** ganar puntos al crear publicaciones, comentar y recibir votos, **para** que mi participación se vea reflejada y recompensada.

**Criterios de aceptación:**
1. Se asignan puntos por: crear publicación, comentar, recibir voto "Me interesa".
2. Los puntos se acumulan en el perfil del usuario.
3. El total de puntos es visible en el perfil.
4. Las reglas de puntos son consistentes y predefinidas.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de niveles e insignias; provee el dato base. |
| Negotiable | Valores de puntos por acción son negociables. |
| Valuable | Motiva la participación activa de los usuarios. |
| Estimable | Triggers en acciones + acumulador de puntos; 2-3 días. |
| Small | Lógica de acumulación + visualización en perfil. |
| Testable | Crear publicación → puntos +X; comentar → puntos +Y; verificar total. |

---

### HU-10.2 — Niveles de usuario
**Como** usuario, **quiero** subir de nivel según mis puntos acumulados (Amigo Animal → Protector → Guardián → Héroe de las Mascotas), **para** ver reconocido mi compromiso con la comunidad.

**Criterios de aceptación:**
1. Existen 4 niveles: Amigo Animal, Protector, Guardián, Héroe de las Mascotas.
2. Cada nivel tiene un umbral de puntos definido.
3. El nivel actual se muestra en el perfil del usuario.
4. Al alcanzar un nuevo nivel, se notifica al usuario.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Depende de HU-10.1 (puntos), pero la lógica de nivel es independiente. |
| Negotiable | Los umbrales de puntos por nivel son negociables. |
| Valuable | Reconocimiento visible y motivación por progresión. |
| Estimable | Lógica de umbrales + UI; 2-3 días. |
| Small | Evaluación de nivel + badge visual + notificación. |
| Testable | Alcanzar umbral → nivel sube; nivel se muestra correctamente en perfil. |

---

### HU-10.3 — Insignias/badges por logros
**Como** usuario, **quiero** recibir insignias al alcanzar logros específicos (primera publicación, 10 publicaciones verificadas, publicación más destacada del mes, etc.), **para** coleccionar reconocimientos y motivarme.

**Criterios de aceptación:**
1. Se definen al menos 3 insignias con criterios claros.
2. Las insignias se otorgan automáticamente al cumplir el criterio.
3. Las insignias obtenidas se muestran en el perfil del usuario.
4. Se notifica al usuario cuando obtiene una nueva insignia.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de niveles; lógica de criterios propia. |
| Negotiable | Cantidad y criterios de insignias son negociables. |
| Valuable | Gamificación que incentiva el uso sostenido. |
| Estimable | Definición de criterios + evaluación + UI; 3-4 días. |
| Small | Lógica de evaluación + colección visual en perfil. |
| Testable | Cumplir criterio → insignia otorgada; visible en perfil; notificación recibida. |

---

## EP-11 — Inteligencia Artificial

**Objetivo:** integrar una funcionalidad basada en un LLM que aporte valor inteligente a la plataforma. Se debe elegir **una** de las cinco opciones disponibles.

### Opción 1: Clasificación automática de categorías

#### HU-11.1a — Sugerencia automática de categoría al crear publicación
**Como** usuario, **quiero** que al crear una publicación el sistema analice el título y la descripción y sugiera automáticamente la categoría más apropiada, **para** agilizar el proceso y reducir errores de categorización.

**Criterios de aceptación:**
1. Al completar título y descripción, el sistema envía el texto al LLM como contexto.
2. El LLM responde con la categoría sugerida (de las 5 existentes).
3. La categoría sugerida se preselecciona en el formulario.
4. El usuario puede aceptar la sugerencia o cambiarla manualmente.
5. Si el servicio LLM no está disponible, el usuario selecciona manualmente sin bloqueo.

---

### Opción 2: Validación de imágenes relevantes

#### HU-11.1b — Validar que las imágenes correspondan a la publicación
**Como** usuario, **quiero** que el sistema valide que las imágenes que adjunto sean relevantes para la categoría y descripción de mi publicación, **para** mantener la calidad del contenido.

**Criterios de aceptación:**
1. Antes de enviar la publicación, las imágenes se envían al LLM (o API de visión) junto con la categoría y descripción.
2. Si las imágenes son relevantes, se permite la publicación.
3. Si no son relevantes, se informa al usuario y se le pide cambiar las imágenes.
4. Si el servicio no está disponible, se permite publicar con advertencia.

---

### Opción 3: Detección de contenido inapropiado

#### HU-11.1c — Analizar texto antes de publicar
**Como** usuario, **quiero** que el sistema analice el texto de mis publicaciones y comentarios antes de publicarlos, **para** evitar contenido ofensivo o inapropiado.

**Criterios de aceptación:**
1. Antes de submitir, el texto se envía al LLM para análisis.
2. Si se detecta contenido problemático, se notifica al usuario y se sugiere una versión alternativa.
3. El usuario puede aceptar la sugerencia, editar manualmente o cancelar.
4. Si el servicio no está disponible, se permite publicar.

---

### Opción 4: Generación de resúmenes para moderadores

#### HU-11.1d — Resumen automático de publicaciones en panel de moderación
**Como** moderador, **quiero** ver un resumen generado automáticamente de cada publicación pendiente (puntos clave, relevancia estimada, acciones recomendadas), **para** revisar múltiples publicaciones de forma rápida y eficiente.

**Criterios de aceptación:**
1. Cada publicación en el panel de moderación muestra un resumen generado por LLM.
2. El resumen incluye: puntos clave del contenido, nivel de relevancia estimado y acciones recomendadas.
3. El resumen se genera automáticamente al cargar el panel o bajo demanda.
4. Si el servicio no está disponible, se muestra la publicación sin resumen.

---

### Opción 5: Detección de publicaciones duplicadas

#### HU-11.1e — Detectar duplicados antes de crear publicación
**Como** usuario, **quiero** que antes de crear mi publicación el sistema la compare semánticamente con publicaciones existentes en la misma zona, **para** evitar duplicados y aportar a publicaciones existentes.

**Criterios de aceptación:**
1. Antes de enviar, el título y descripción se comparan semánticamente con publicaciones existentes en la misma zona geográfica.
2. Si se detectan posibles duplicados, se muestra una lista al usuario.
3. El usuario puede elegir aportar información a una publicación existente o crear una nueva.
4. Si no se detectan duplicados, la publicación se crea normalmente.
5. Si el servicio no está disponible, se permite crear sin verificación de duplicados.

---

**INVEST (aplica a todas las opciones de IA):**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente del flujo base; se integra como paso adicional. |
| Negotiable | LLM a usar, umbrales de confianza y UX del flujo son negociables. |
| Valuable | Agrega inteligencia y mejora la calidad del contenido o la eficiencia. |
| Estimable | Integración con API de LLM + lógica de flujo; 5-7 días. |
| Small | Un paso adicional en un flujo existente (creación o moderación). |
| Testable | Se prueba con textos/imágenes que deben pasar y que deben ser rechazados/sugeridos; fallback sin servicio. |

---

## EP-12 — Internacionalización (i18n)

**Objetivo:** soportar múltiples idiomas en la interfaz de la aplicación.

### HU-12.1 — Soporte multi-idioma en la interfaz
**Como** usuario, **quiero** que la aplicación esté disponible en al menos dos idiomas, **para** poder usarla en mi idioma preferido.

**Criterios de aceptación:**
1. Todos los textos de la interfaz están externalizados en archivos de recursos de cadenas (`strings.xml`).
2. Se soportan al menos dos idiomas (ej.: español e inglés).
3. El idioma se puede cambiar desde la configuración de la app o sigue el del dispositivo.
4. Los contenidos generados por usuarios (publicaciones, comentarios) no se traducen.

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente de la lógica funcional. |
| Negotiable | Idiomas soportados y mecanismo de cambio son negociables. |
| Valuable | Amplía la accesibilidad a usuarios de otros idiomas. |
| Estimable | Externalización de strings + archivos de traducción; 3-5 días. |
| Small | Archivos de recursos + configuración. |
| Testable | Cambiar idioma → toda la UI se muestra en el nuevo idioma. |

---

## EP-13 — Funcionalidad Adicional No Trivial

**Objetivo:** diseñar e implementar una funcionalidad adicional que agregue valor diferenciador a la plataforma, según lo definido por el equipo.

### HU-13.1 — Funcionalidad adicional (por definir por el equipo)
**Como** equipo de desarrollo, **debemos** proponer e implementar una funcionalidad adicional no trivial, **para** cumplir con el requisito del proyecto y aportar valor diferenciador a PetHelp.

**Criterios de aceptación:**
1. La funcionalidad debe ser no trivial (no puede ser un cambio cosmético o menor).
2. Debe estar alineada con la temática de adopción de mascotas.
3. Debe integrarse de forma coherente con el resto de la aplicación.
4. Debe documentarse en las épicas una vez definida (se descompondrá en HU INVEST propias).

**INVEST:**
| Criterio | Cumplimiento |
|---|---|
| Independent | Independiente del resto de épicas. |
| Negotiable | La funcionalidad misma es completamente negociable. |
| Valuable | Agrega diferenciación y cumple requisito de evaluación. |
| Estimable | Estimable una vez definida la funcionalidad concreta. |
| Small | Se descompondrá en HU pequeñas cuando se defina. |
| Testable | Se definirán criterios de aceptación específicos al concretar la funcionalidad. |

---

## Resumen de épicas

| ID | Épica | Total HU |
|---|---|---|
| EP-01 | Autenticación y Gestión de Cuentas | 6 |
| EP-02 | Gestión de Publicaciones de Mascotas | 4 |
| EP-03 | Feed y Exploración | 4 |
| EP-04 | Moderación de Contenido | 4 |
| EP-05 | Interacción Social y Adopción | 3 |
| EP-06 | Notificaciones en Tiempo Real | 3 |
| EP-07 | Mapas y Geolocalización | 2 |
| EP-08 | Gestión de Imágenes | 2 |
| EP-09 | Estadísticas y Dashboard Personal | 1 |
| EP-10 | Sistema de Reputación y Logros | 3 |
| EP-11 | Inteligencia Artificial (elegir 1 opción) | 1 |
| EP-12 | Internacionalización | 1 |
| EP-13 | Funcionalidad Adicional No Trivial | 1 (por descomponer) |
| | **Total** | **35** |

---

## Asignación por fases del proyecto

### Fase 1 — Diseño
- Mockups de todas las pantallas de todas las épicas (EP-01 a EP-13).

### Fase 2 — Funcionalidades básicas (datos en memoria)
- EP-01: Registro, login, perfil (sin Firebase; datos en memoria).
- EP-02: CRUD de publicaciones (datos en memoria).
- EP-03: Feed lista y detalle (datos mock).
- EP-04: Panel de moderación (datos mock).
- EP-05: Comentarios y votos (datos en memoria).
- EP-09: Dashboard (datos calculados en memoria).
- EP-10: Puntos, niveles, insignias (lógica en memoria).

### Fase 3 — Funcionalidades completas
- EP-01: Firebase Auth + recuperación de contraseña.
- EP-02: Persistencia en BD.
- EP-03: Feed en mapa + filtros con BD real.
- EP-04: Moderación con persistencia.
- EP-05: Persistencia + notificaciones al comentar.
- EP-06: FCM + notificaciones georreferenciadas + lista de notificaciones.
- EP-07: Integración completa de mapas.
- EP-08: Subida de imágenes a servicio externo.
- EP-11: Integración con LLM.
- EP-12: Internacionalización.
- EP-13: Funcionalidad adicional.
