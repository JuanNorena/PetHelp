package com.pethelp.app.features.chat.presentation

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.pethelp.app.R
import com.pethelp.app.core.navigation.Screen
import com.pethelp.app.core.ui.components.PetHelpBottomNavBar

private data class ConversationPreview(
    val id: String,
    val name: String,
    val subject: String,
    val lastMessage: String,
    val timeLabel: String,
    val updatedAtMillis: Long,
    val unreadCount: Int,
    val accentTag: String
)

private data class ChatMessage(
    val id: String,
    val text: String,
    val isMine: Boolean,
    val timeLabel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val conversations = remember { mutableStateListOf<ConversationPreview>() }
    var totalUnreadCount by rememberSaveable { mutableStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var loadError by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    DisposableEffect(uid) {
        if (uid.isBlank()) {
            conversations.clear()
            totalUnreadCount = 0
            onDispose { }
        } else {
            val query = FirebaseFirestore.getInstance()
                .collection("threads")
                .whereArrayContains("participants", uid)

            val listener = query.addSnapshotListener { snap, err ->
                if (err != null) {
                    loadError = err.localizedMessage
                    return@addSnapshotListener
                }

                val previews = snap?.documents
                    ?.map { it.toConversationPreview(uid, context.getString(R.string.chat_thread_title)) }
                    ?.sortedByDescending { it.updatedAtMillis }
                    .orEmpty()

                conversations.clear()
                conversations.addAll(previews)
                totalUnreadCount = previews.sumOf { it.unreadCount }
                loadError = null
            }

            onDispose { listener.remove() }
        }
    }

    val visibleConversations = remember(conversations.toList(), searchQuery) {
        if (searchQuery.isBlank()) {
            conversations.toList()
        } else {
            val query = searchQuery.trim()
            conversations.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.subject.contains(query, ignoreCase = true) ||
                    it.lastMessage.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.chat_title),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(R.string.chat_subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.chat_search_hint)
                        )
                    }
                }
            )
        },
        bottomBar = {
            PetHelpBottomNavBar(navController = navController, unreadChatCount = totalUnreadCount)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.chat_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            }

            item {
                ChatIntroCard()
            }

            if (loadError != null) {
                item {
                    Text(
                        text = loadError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (visibleConversations.isEmpty()) {
                item {
                    EmptyConversationState()
                }
            } else {
                items(visibleConversations, key = { it.id }) { conversation ->
                    ConversationCard(
                        conversation = conversation,
                        onClick = { navController.navigate(Screen.ChatThread(conversation.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatIntroCard() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.chat_about_adoption),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.chat_placeholder_contact),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun EmptyConversationState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.chat_empty_title),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.chat_empty_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationPreview,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (conversation.unreadCount > 0)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.26f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.name.firstOrNull()?.uppercase() ?: "P",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.name,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = conversation.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = conversation.subject,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = conversation.lastMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (conversation.accentTag.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = conversation.accentTag,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (conversation.unreadCount > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 9) "+9" else conversation.unreadCount.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadScreen(
    navController: NavController,
    threadId: String
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val messages = remember(threadId) { mutableStateListOf<ChatMessage>() }
    var draft by rememberSaveable(threadId) { mutableStateOf("") }
    var isSendingMessage by rememberSaveable { mutableStateOf(false) }
    var unreadChatCount by rememberSaveable { mutableStateOf(0) }
    var threadTitle by rememberSaveable(threadId) { mutableStateOf(context.getString(R.string.chat_thread_title)) }

    DisposableEffect(uid) {
        if (uid.isBlank()) {
            onDispose { }
        } else {
            val threadsQuery = db.collection("threads").whereArrayContains("participants", uid)
            val threadsListener = threadsQuery.addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                unreadChatCount = snap?.documents?.sumOf { it.unreadFor(uid) } ?: 0
            }
            onDispose { threadsListener.remove() }
        }
    }

    DisposableEffect(threadId, uid) {
        if (uid.isNotBlank()) {
            db.collection("threads")
                .document(threadId)
                .set(mapOf("unreadByUser" to mapOf(uid to 0)), SetOptions.merge())
        }
        onDispose { }
    }

    DisposableEffect(threadId) {
        val threadRef = db.collection("threads").document(threadId)
        val listener = threadRef.addSnapshotListener { snap, err ->
            if (err != null || snap == null || !snap.exists()) return@addSnapshotListener
            threadTitle = snap.getString("title") ?: context.getString(R.string.chat_thread_title)
        }
        onDispose { listener.remove() }
    }

    DisposableEffect(threadId) {
        val messagesQuery = db.collection("threads")
            .document(threadId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val listener = messagesQuery.addSnapshotListener { snap, err ->
            if (err != null) return@addSnapshotListener
            messages.clear()
            snap?.documents?.forEach { doc ->
                val text = doc.getString("text").orEmpty()
                val author = doc.getString("authorId").orEmpty()
                val timeMillis = doc.get("createdAt").toMillis()
                messages.add(
                    ChatMessage(
                        id = doc.id,
                        text = text,
                        isMine = author == uid,
                        timeLabel = if (timeMillis > 0L) formatRelative(timeMillis) else doc.getString("timeLabel") ?: ""
                    )
                )
            }
        }
        onDispose { listener.remove() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = threadTitle, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = stringResource(R.string.chat_thread_context, threadId),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            PetHelpBottomNavBar(navController = navController, unreadChatCount = unreadChatCount)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.chat_start_adoption_flow),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.chat_empty_subtitle),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                        shape = RoundedCornerShape(28.dp),
                        maxLines = 4,
                        enabled = !isSendingMessage
                    )
                    Spacer(Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            val text = draft.trim()
                            if (text.isBlank() || isSendingMessage || uid.isBlank()) return@IconButton

                            isSendingMessage = true
                            draft = ""
                            sendChatMessage(
                                db = db,
                                threadId = threadId,
                                senderId = uid,
                                text = text,
                                onSuccess = {
                                    isSendingMessage = false
                                },
                                onFailure = { error ->
                                    isSendingMessage = false
                                    draft = text
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.chat_error_sending, error.localizedMessage ?: "Desconocido"),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                if (isSendingMessage) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        enabled = !isSendingMessage
                    ) {
                        if (isSendingMessage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = stringResource(R.string.chat_send),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = if (message.isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    color = if (message.isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message.timeLabel,
                    color = if (message.isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(if (message.isMine) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}

private fun sendChatMessage(
    db: FirebaseFirestore,
    threadId: String,
    senderId: String,
    text: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val threadRef = db.collection("threads").document(threadId)
    threadRef.get()
        .addOnSuccessListener { snap ->
            val participants = snap.getStringListSafe("participants")
            val now = FieldValue.serverTimestamp()
            val messageRef = threadRef.collection("messages").document()
            val unreadByUser = participants.associateWith { participantId ->
                if (participantId == senderId) 0 else FieldValue.increment(1)
            }

            val batch = db.batch()
            batch.set(
                messageRef,
                mapOf(
                    "id" to messageRef.id,
                    "text" to text,
                    "authorId" to senderId,
                    "createdAt" to now,
                    "timeLabel" to "Ahora"
                )
            )
            batch.set(
                threadRef,
                mapOf(
                    "lastMessage" to text,
                    "lastSenderId" to senderId,
                    "updatedAt" to now,
                    "lastMessageAt" to now,
                    "unreadByUser" to unreadByUser
                ),
                SetOptions.merge()
            )
            batch.commit()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        }
        .addOnFailureListener { onFailure(it) }
}

private fun DocumentSnapshot.toConversationPreview(currentUserId: String, fallbackTitle: String): ConversationPreview {
    val updatedAt = get("updatedAt").toMillis()
    val lastMessage = getString("lastMessage")
        ?.takeIf { it.isNotBlank() }
        ?: "Aun no hay mensajes."
    val title = getString("title")
        ?.takeIf { it.isNotBlank() }
        ?: fallbackTitle
    val subtitle = getString("subtitle")
        ?.takeIf { it.isNotBlank() }
        ?: "Chat de adopcion"
    val tag = getString("tag")
        ?.takeIf { it.isNotBlank() }
        ?.replaceFirstChar { it.uppercase() }
        ?: ""

    return ConversationPreview(
        id = id,
        name = title,
        subject = subtitle,
        lastMessage = lastMessage,
        timeLabel = if (updatedAt > 0L) formatRelative(updatedAt) else "",
        updatedAtMillis = updatedAt,
        unreadCount = unreadFor(currentUserId),
        accentTag = tag
    )
}

private fun DocumentSnapshot.unreadFor(uid: String): Int {
    val unreadByUser = get("unreadByUser") as? Map<*, *>
    val ownUnread = unreadByUser?.get(uid)
    return when (ownUnread) {
        is Long -> ownUnread.toInt()
        is Double -> ownUnread.toInt()
        is Int -> ownUnread
        else -> (getLong("unreadCount") ?: 0L).toInt()
    }
}

private fun DocumentSnapshot.getStringListSafe(field: String): List<String> {
    return (get(field) as? List<*>)
        ?.mapNotNull { it as? String }
        .orEmpty()
}

private fun Any?.toMillis(): Long {
    return when (this) {
        is Timestamp -> toDate().time
        is Long -> this
        is Double -> toLong()
        is Int -> toLong()
        else -> 0L
    }
}

private fun formatRelative(timeMillis: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timeMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
