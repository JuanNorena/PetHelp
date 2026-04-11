package com.pethelp.app.features.post.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pethelp.app.R
import com.pethelp.app.core.common.Resource
import com.pethelp.app.core.domain.model.Post
import com.pethelp.app.core.domain.model.PostStatus
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.theme.PetHelpPrimary
import com.pethelp.app.core.ui.theme.PetHelpSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(
    navController: NavController,
    viewModel: MyPostsViewModel = hiltViewModel()
) {
    val postsState by viewModel.postsState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Activas", "En revisión", "Finalizadas")

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Publicaciones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF101828)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .background(Color(0xFFE8F5E9), CircleShape)
                            .clip(CircleShape)
                            .clickable { navController.navigate(Screen.CreatePost) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo",
                            tint = PetHelpPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreatePost) },
                containerColor = PetHelpSecondary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Publicar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9FAFB))
        ) {
            // ── Tabs de Filtro ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color(0xFFF2F4F7),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val selected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) Color.White else Color.Transparent)
                                .then(if (selected) Modifier.shadow(if (selected) 1.dp else 0.dp, RoundedCornerShape(20.dp)) else Modifier)
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                color = if (selected) Color(0xFF101828) else Color(0xFF667085),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ── Lista de Publicaciones ──
            when (val state = postsState) {
                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PetHelpPrimary)
                    }
                }
                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message ?: "Error desconocido", color = Color.Red)
                    }
                }
                is Resource.Success -> {
                    val allPosts = state.data ?: emptyList()
                    val filteredPosts = when (selectedTab) {
                        0 -> allPosts.filter { it.status == PostStatus.VERIFIED }
                        1 -> allPosts.filter { it.status == PostStatus.PENDING || it.status == PostStatus.REJECTED }
                        else -> allPosts.filter { it.status == PostStatus.RESOLVED }
                    }

                    if (filteredPosts.isEmpty()) {
                        EmptyState(tabs[selectedTab])
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredPosts) { post ->
                                MyPostCard(
                                    post = post,
                                    onEdit = { navController.navigate(Screen.EditPost(post.id)) },
                                    onDelete = { viewModel.deletePost(post.id) },
                                    onToggleStatus = { viewModel.togglePostStatus(post.id, it) },
                                    onMarkResolved = { viewModel.markAsResolved(post.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyPostCard(
    post: Post,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: (Boolean) -> Unit,
    onMarkResolved: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF2F4F7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Imagen
                AsyncImage(
                    model = post.imageUrls.firstOrNull() ?: R.drawable.img_happy_puppy,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(16.dp))

                // Info principal
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101828)
                        )
                        StatusBadge(post.status)
                    }

                    Text(
                        text = "${post.animalType} • ${post.breed}",
                        fontSize = 14.sp,
                        color = Color(0xFF667085)
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF99A1AF), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(post.locationName, fontSize = 13.sp, color = Color(0xFF667085))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF99A1AF), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        val date = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(post.createdAt))
                        Text(date, fontSize = 13.sp, color = Color(0xFF667085))
                    }
                }
            }

            // Banner de Rechazo (Imagen 2 del usuario)
            if (post.status == PostStatus.REJECTED) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFBFA), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFFDA29B), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFD92D20), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        post.rejectionReason ?: "Tu publicación no cumple con nuestras normas. Por favor edítala.",
                        color = Color(0xFFB42318),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = Color(0xFFF2F4F7))

            // Acciones y Métricas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Iconos de acciones a la izquierda
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(
                        Icons.Outlined.Edit,
                        null,
                        tint = Color(0xFF667085),
                        modifier = Modifier.size(22.dp).clickable { onEdit() }
                    )
                    
                    // Solo mostramos Pausa/Play si está Activa o en Revisión (pero no Rechazada)
                    if (post.status == PostStatus.VERIFIED || post.status == PostStatus.PENDING) {
                        val isPaused = post.status == PostStatus.PENDING
                        Icon(
                            if (isPaused) Icons.Outlined.PlayCircleOutline else Icons.Outlined.PauseCircleOutline,
                            null,
                            tint = Color(0xFF667085),
                            modifier = Modifier.size(22.dp).clickable { onToggleStatus(!isPaused) }
                        )
                    }
                }

                // Botón "Adoptado" central (solo si es activa)
                if (post.status == PostStatus.VERIFIED) {
                    Button(
                        onClick = onMarkResolved,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECFDF3), contentColor = Color(0xFF027A48)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Adoptado", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Icono de eliminar a la derecha
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Delete, null, tint = Color(0xFFFDA29B), modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: PostStatus) {
    val (color, text) = when (status) {
        PostStatus.VERIFIED -> Color(0xFF027A48) to "ACTIVA"
        PostStatus.PENDING -> Color(0xFFF79009) to "EN REVISIÓN"
        PostStatus.REJECTED -> Color(0xFFD92D20) to "RECHAZADA"
        PostStatus.RESOLVED -> Color(0xFF7F56D9) to "ADOPTADA"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyState(tabName: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues = PaddingValues(top = 80.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🐾", fontSize = 48.sp)
            Text(
                "No tienes publicaciones en $tabName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF101828)
            )
            Text(
                "Tus publicaciones aparecerán aquí una vez que las crees.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color(0xFF667085),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}
