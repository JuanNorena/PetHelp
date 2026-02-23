# Guía de Diseño y Prompts para Figma Make - PetHelp

Este documento contiene las especificaciones detalladas de diseño y los **prompts** (instrucciones) creados específicamente para ser ingresados en **Figma Make** (u otra IA generadora de UI) con el fin de crear los mockups del proyecto PetHelp.

La meta principal es lograr un diseño con estilo **Material You (Material Design 3)** que sea moderno, altamente atractivo, vibrante y centrado en transmitir empatía y protección hacia los animales.

---

## 🎨 1. Guía de Estilos General (Contexto Global)

Antes de generar las pantallas específicas, es fundamental proporcionar este contexto a la IA de diseño para asegurar coherencia visual en todo el proyecto.

- **Estilo Visual:** Material You / Material Design 3. Fuerte uso de bordes muy redondeados (_smooth roundings_), tarjetas elevadas con sombras suaves (_soft drop shadows_) y diseño adaptable para modo claro (_Light Mode_) y modo oscuro (_Dark Mode_).
- **Tipografía:** Fuentes sin serifas (_sans-serif_) modernas, limpias e iterativas. **Sugeridas:** Outfit, Inter o Roboto. Debe haber una excelente jerarquía visual (encabezados grandes en negrita, subtítulos claros, texto de cuerpo muy legible).
- **Formas y Componentes:** Botones prominentes y curvos (esquinas _pill-shaped_ o "Full Rounded"). _Floating Action Buttons_ (FABs) grandes, y campos de formulario (_Text Fields_) de estilo _Outlined_ con esquinas curvas e interactivas.
- **Layout & Espaciado:** Interfaz abierta y limpia, utilizando abundante espacio en blanco (_whitespace_ / _negative space_) para que todos los elementos respiren. Evitar estrictamente interfaces saturadas o abarrotadas. Márgenes y _paddings_ consistentes.
- **Tono Emocional:** La aplicación debe evocar amabilidad, seguridad, confianza y profesionalismo, sin perder un toque lúdico y tierno relacionado con las mascotas.

---

## 🌈 2. Paleta de Colores Dinámica (Material You)

La paleta se inspira en colores extraídos de la naturaleza, ideales para evocar cuidado y protección animal:

- **Color Primario:** Verde Salvia Suave (`#4CAF50` o similares terrosos/vibrantes) o Turquesa cálido (`#00BFA5`). Representan naturaleza, sanación, vida y crecimiento.
- **Color Secundario:** Naranja o Coral suave (`#FF8A65`). Aporta calidez y mucha energía. Se usa principalmente para atrapar la atención (ideal para botones de _"Me interesa adoptar"_).
- **Color Terciario:** Púrpura o Violeta pastel (`#B39DDB`). Se usará de forma muy sutil para resaltar detalles premium o intervenciones de Inteligencia Artificial (ej. etiquetas de _"Resumen IA"_).
- **Superficies / Fondos:** En modo luz (_Light Mode_) usar blanco puro o crema muy claro (`#FAFAFA` / `#F5F5F7`); en modo oscuro (_Dark Mode_), usar grises profundos en lugar de negro puro (`#1E1E1E` / `#121212`).
- **Alertas / Errores:** Rojo pastel amigable (`#E57373`) para advertencias o para rechazar publicaciones.

---

## 📱 3. Prompts Detallados por Pantalla

Copia el texto dentro de los bloques para generar las pantallas una a una en Figma Make.

### 3.1 Flujo de Autenticación

#### Pantalla 1: Splash Screen & Bienvenida

> "Genera una pantalla móvil moderna de Splash/Bienvenida estilo Material Design 3 para una app llamada 'PetHelp'. El fondo debe ser un color crema muy suave o verde salvia pastel. En el centro, ilustra un logotipo abstracto y moderno uniendo un corazón, unas manos protectoras y una pata de mascota, con un ligero efecto glassmorphism. Abajo, el título 'PetHelp' en tipografía geométrica (ej. Outfit Bold) gruesa y atractiva. Debajo del título, el lema 'Encuentra, cuida, adopta' en gris medio. En la parte inferior de la pantalla, un botón primario grande redondeado que diga 'Comenzar'."

#### Pantalla 2: Inicio de Sesión (Login)

> "Crea una pantalla de Login móvil para la app 'PetHelp' con estilo Material You, moderna y muy limpia. Cabecera: Un texto grande y amigable '¡Hola de nuevo!' y una pequeña ilustración 3D animada (o vectorial suave) de un perrito o gatito feliz asomándose. Centro: Dos campos de texto estilo Outlined Text Field de Material 3, con bordes redondeados, para 'Correo Electrónico' y 'Contraseña' (con icono de ojo). Debajo de la contraseña, alineado a la derecha, un enlace morado sutil que diga '¿Olvidaste tu contraseña?'. Acciones: Un botón principal grande, vibrante (color coral interactivo o verde salvia) estilo pill (totalmente redondeado) que diga 'Iniciar Sesión'. Pie de página: '¿No tienes cuenta? Regístrate', con la palabra 'Regístrate' resaltada en el color primario."

#### Pantalla 3: Registro (Sign Up)

> "Diseña una pantalla móvil de Registro para la app 'PetHelp', estética Material 3. Cabecera con botón de 'Flecha Atrás' circular a la izquierda, y un título grande 'Crea tu cuenta'. Formulario central limpio y espacioso: Campos Outlined Text Field para 'Nombre completo', 'Correo electrónico', y 'Contraseña'. Debajo del campo de contraseña, una barra de progreso que indique la seguridad de la contraseña ('Fortaleza: Fuerte') en color verde. Un checkbox elegante tipo cruz/check redondo con texto 'Acepto los términos y condiciones'. Al final, un botón primario muy llamativo e inflado (chubby) con sombra suave que diga 'Registrarme'."

#### Pantalla 4: Recuperación de Contraseña

> "Crea una pantalla móvil de Recuperación de Contraseña para la app 'PetHelp' con estilo Material You 3. Cabecera: Botón de 'Atrás' circular a la izquierda. Título grande y empático: '¿Olvidaste tu contraseña?'. Debajo del título, un texto descriptivo amigable en color gris: 'No te preocupes. Ingresa tu correo electrónico y te enviaremos un enlace para recuperar el acceso a tu cuenta.'. Centro: Un campo Outlined Text Field grande y redondeado para 'Correo Electrónico', con un ícono de sobre (email) en su interior a la izquierda. Abajo: Un botón primario ancho (full-width) y totalmente redondeado (pill shape) que diga 'Enviar enlace', en color primario vibrante (verde salvia o coral). De manera opcional, incluye una ilustración minimalista y tierna, estilo vector, de una llave o un candado acompañado de una silueta de mascota."

---

### 3.2 Flujo Principal de Usuario (Feed y Exploración)

#### Pantalla 5: Feed Principal (Vista Lista - Home)

> "Diseña el Home / Feed en vista de lista de una app de mascotas estilo Material 3. Top App Bar: Título alineado a la izquierda 'PetHelp' y a la derecha un ícono de campana (notificaciones) y un avatar de usuario circular pequeño. Fila horizontal deslizable debajo del Header con 'Chips' redondeados estilo Material 3 coloreados sutilmente: '🐾 Adopción', '🔍 Perdidos', '📍 Encontrados', '🏠 Hogar temporal', '💉 Veterinaria'.
>
> Contenido principal: Lista vertical de tarjetas (elevated cards) grandes y muy atractivas. Cada tarjeta debe mostrar: 1) Una foto panorámica o cuadrada de alta calidad de un perro o gato ocupando la parte superior, esquinas curvas. 2) Flotando sobre una esquina de la foto, una píldora semitransparente (blur/glass) indicando la distancia '📍 a 2 km'. 3) Debajo de la foto, área de texto de la tarjeta: Título grande y negrita (Ej. 'Max - Golden Retriever'), abajo unos tags sutiles grises con iconos ('Macho', 'Mediano', 'Medellín'). 4) En la esquina inferior derecha de la tarjeta, un botón de icono (IconButton) con una estrella o corazón acompañado del número de votos interes ('12').
>
> Pantalla inferior: Bottom Navigation Bar moderno, con píldoras indicando elemento activo (Home o Feed, Mapa, Nueva Pub (botón central más grande estilo FAB flotante), Mensajes/Notifs, Perfil)."

#### Pantalla 6: Feed Principal (Vista Mapa)

> "Diseña una pantalla móvil de Mapa inmersiva estilo Google Maps pero colores vibrantes adaptados al estilo Material You de la app de mascotas 'PetHelp'. El mapa ocupa todo el fondo. En la parte superior, flotando, una barra de búsqueda gruesa redondeada (SearchBar) que incluye un icono de menú hamburguesa a la vez, y debajo una fila horizontal de 'Filter Chips' (Adopción, Perdidos...).
>
> Sobre el mapa, distribuye marcadores de ubicación personalizados (custom pins): En lugar del clásico pin rojo, deben ser pequeños círculos blancos o verdes salvia con una foto en miniatura de la cara de la mascota, o vectores.
>
> Abajo, sobrepuesto al mapa, un Bottom Sheet (Panel inferior superpuesto) asomándose levemente mostrando una tarjeta de resumen horizontal de una mascota seleccionada en el mapa: Foto cuadrada izquierda, 'Luna - Perdida en Av. San Martín' a la derecha, botón pequeño de 'Ver detalle'. Bottom Navigation Bar fijo abajo del todo."

---

### 3.3 Detalle e Interacción

#### Pantalla 7: Detalle de Publicación de Adopción

> "Crea una vista detallada inmersiva de una publicación de mascota para la app 'PetHelp' en Material You. Superior: Imagen de cabecera expansiva (hero image) del perro o gato ocupando la zona superior de fondo (aprox 40% del alto), con un botón de retroceso flotante traslúcido arriba a la izquierda.
>
> Panel de contenido: Se súper-impone a la imagen desde abajo con esquinas superiores muy redondeadas (Bottom sheet pattern). Título espectacular gigante 'Kira'. A la derecha del título, nivelados, un avatar pequeño circular del usuario publicador con su nombre 'Por JuanP'. Segmento de características de la mascota presentado como una cuadrícula de rectángulos color pastel pequeños y limpios con un ícono + texto en cada celda: 'Hembra', 'Schnauzer', 'Mediano', 'Vacunada'. Bloque de texto de descripción amigable con buen track y leading. Un minimapa rectangular pequeño abajo que muestra un pin rojo de donde se encuentra. Un fragmento pequeño para comentarios: avatar + input tipo text field 'Escribe un comentario...'.
>
> Pie de página (Sticky bottom bar) muy importante con alta elevación, uniendo la pantalla completa en la base: Tiene un texto que dice 'Votos: 45' y un botón de lado a lado gigantesco y sumamente llamativo color Naranja/Coral o Primary (call to action principal) con un ícono de patita y el texto: 'Solicitar Adopción', usando botones llenos (Filled Button) de Material 3 con curvas."

---

### 3.4 Flujo de Creación (Formulario con IA)

#### Pantalla 8: Crear Publicación (Subida de fotos e IA)

> "Diseña la pantalla de 'Nueva Publicación' para la app PetHelp, estética Material You espaciosa. Top App bar dice 'Ayuda a un peludo'. El primer gran componente central es un recuadro grande delineado (Dashed border o relleno muy sutil pastel) con un enorme ícono de cámara o imagen en el centro que diga 'Toca para agregar hasta 5 fotos'. Dentro, mockupea dos fotos pequeñas añadidas y un botón circular pequeño con un '+' .
>
> A continuación, campos del formulario OutlinedTextField gruesos y redondeados: 'Título de publicación' y un text-area grande para 'Descripción de la situación'.
>
> Un componente especial debajo: Un campo selector ('Dropdown Menu' o 'Exposed Dropdown Menu' M3) titulado 'Categoría sugerida por IA'. Acompáñalo de chispitas mágicas (sparkles icon) en morado claro que indique que la Inteligencia Artificial analizó la foto/texto y pre-seleccionó 'Adopción' automáticamente. Secciones siguientes con 'Choice Chips' seleccionables redondos para seleccionar tipo (Perro, Gato, Otro) y Tamaño. Botón de acción principal abajo fijado a la base que dice 'Siguiente: Ubicación'."

---

### 3.5 Panel de Moderador y Resumen de IA

#### Pantalla 9: Bandeja de Moderación (Moderator Panel)

> "Crea el 'Panel de Moderador' para la app PetHelp. Este usuario revisa las publicaciones pendientes. Estética Material M3 enfocada en utilidad. App bar: 'Bandeja de Entrada - Pendientes'.
>
> El lienzo es gris muy sutil o crema. Contiene una lista vertical de Tarjetas tipo 'ListItem' engrosadas. Cada tarjeta tiene: Izquierda: Foto cuadrada diminuta (thumbnail). Centro-Derecha: Columna apilada. 'Buscamos hogar para Max - Adopción'. Un 'Tag' de IA (etiqueta violeta especial con bordes redondeados, ícono de estrella o varita mágica) con texto sugerido generado automáticamente: 'Resumen IA: Posible adopción real. Imagen coincide con un Border Collie. Ningún tono de venta detectado'. A la derecha, o integrados muy notoriamente, dos grandes botones en forma de píldora (o IconButton circulares M3 Filled Tonal): Un botón verde con un Checkmark claro (Aprobar) y un botón rojo pastel (Superficie Error M3) cruzando (Rechazar). La interfaz debe ser limpia para gestionar múltiples revisiones rápidamente."

#### Pantalla 10: Diálogo - Rechazar Publicación

> "Genera un componente Modal Bottom Sheet (Panel inferior emergente) de Material Design 3 llamado 'Rechazar Publicación' sobrepuesta a otra UI difuminada de fondo. Diseño del modal: Color blanco u oscuro puro con curvas top-left/top-right muy amplias. Arriba en centro una barrita gris horizontal de arrastre (drag handle). Título: 'Motivo del Rechazo' en negrita (Headline 6 / Title Large M3). Un enorme campo Outlined para texto que indique texto placehoder 'Explica respetuosamente al usuario por qué la publicación no puede ser aprobada...'. Abajo un texto diminuto auxiliar 'Este motivo será enviado por el autor.' Botones inferiores a la derecha: 'Cancelar' en botón tipo Text-Button, y 'Rechazar' en botón relleno color rojo oscuro o marrón (Filled Button Error color)."

---

### 3.6 Notificaciones y Dashboard Personal

#### Pantalla 11: Pantalla de Notificaciones

> "Diseña una interfaz de Notificaciones moderna y estructurada, similar al estilo de Material You, de bandeja de correos. List items uno sobre el otro sin divisiones abruptas. Agrupados por labels de texto pequeño como 'Nuevas' y 'Ayer'. Formato de cada notificación: A la izquierda un avatar circular de M3 con foto o con logo de campana. Al centro un texto de 2 líneas descriptivo (Ej: 'Tu publicación ha sido verificada y ya está pública' o 'Santi ha comentado en Max - Golden Retriever'). A la derecha la hora 'Hace 2h'. El fondo de las notificaciones NO leídas (nuevas) tiene un sombreado o relleno levísimo de un color primario pastel muy bajito. Diseño minimalista y extremadamente fácil de leer."

#### Pantalla 12: Perfil de Gamificación de Usuario (Dashboard Puntos)

> "Crea el Perfil / Dashboard gamificado de un usuario nivel alto para PetHelp, material you style. Arriba, la sección del avatar grande. Alrededor de la cara del perfil, un aro gradiente de progreso de nivel, dorado o coral vibrante. Debajo su nombre y un Badge gráfico debajo del nombre que dice '👑 Héroe de las Mascotas'. Contenido principal en cajas cuadradas o grid cards gruesas: Una tarjeta de elevación M3 que diga '1,450 puntos' gigantescos, con íconos de llama o estrella a los lados. Otra tarjeta o grid a dos columnas: 'Publicaciones Activas: 4', 'Solicitudes recibidas: 12'. Una sección abajo titulada 'Tus Insignias'. Lista deslizable tipo carrusel horizontal o wrap layout que muestra Escudos vectoriales renderizados (Badges 3D sutiles) logrados: Insignia 1: 'Voluntario Novato', Insignia 2: 'Diez publicaciones validadas', Insignia 3: 'Más popular del mes'. El diseño tiene que ser lúdico pero con la seriedad y calidad de Material Design 3, nada de estilo barato mobile game. Tiene que verse premium."
