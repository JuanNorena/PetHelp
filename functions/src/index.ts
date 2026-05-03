import * as admin from "firebase-admin";
import { onDocumentCreated, onDocumentUpdated } from "firebase-functions/v2/firestore";
import { https } from "firebase-functions/v2";
import { logger } from "firebase-functions";

admin.initializeApp();
const db = admin.firestore();

const COLLECTIONS = {
  posts: "posts",
  comments: "comments",
  notifications: "notifications",
  users: "users",
  fcmTokens: "fcmTokens",
} as const;

type NotificationType =
  | "NEW_POST_NEARBY"
  | "NEW_COMMENT"
  | "POST_APPROVED"
  | "POST_REJECTED";

async function createNotification(params: {
  userId: string;
  type: NotificationType;
  title: string;
  body: string;
  relatedPostId?: string;
  metadata?: Record<string, unknown>;
}): Promise<string> {
  const ref = db.collection(COLLECTIONS.notifications).doc();
  await ref.set({
    userId: params.userId,
    type: params.type,
    title: params.title,
    body: params.body,
    relatedPostId: params.relatedPostId ?? null,
    isRead: false,
    metadata: params.metadata ?? {},
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  return ref.id;
}

async function sendPushIfEnabled(params: {
  userId: string;
  title: string;
  body: string;
  relatedPostId?: string;
  notificationId?: string;
}) {
  const userRef = db.collection(COLLECTIONS.users).doc(params.userId);
  const userSnap = await userRef.get();

  if (!userSnap.exists) return;

  const userData = userSnap.data() ?? {};
  const pushEnabled = userData.pushNotificationsEnabled !== false;
  if (!pushEnabled) return;

  const tokensSnap = await userRef
    .collection(COLLECTIONS.fcmTokens)
    .where("enabled", "==", true)
    .get();

  const tokens = tokensSnap.docs
    .map((d) => d.get("token"))
    .filter((t): t is string => typeof t === "string" && t.length > 0);

  if (tokens.length === 0) return;

  const message: admin.messaging.MulticastMessage = {
    tokens,
    notification: {
      title: params.title,
      body: params.body,
    },
    data: {
      relatedPostId: params.relatedPostId ?? "",
      notificationId: params.notificationId ?? "",
      title: params.title,
      body: params.body,
    },
    android: {
      priority: "high",
      notification: {
        channelId: "pethelp_channel",
      },
    },
  };

  const response = await admin.messaging().sendEachForMulticast(message);

  const invalidTokens: string[] = [];
  response.responses.forEach((res, idx) => {
    if (!res.success) {
      const code = res.error?.code ?? "unknown";
      if (code.includes("registration-token-not-registered") || code.includes("invalid-argument")) {
        invalidTokens.push(tokens[idx]);
      }
      logger.warn("Failed push", { userId: params.userId, code, token: tokens[idx] });
    }
  });

  if (invalidTokens.length > 0) {
    const batch = db.batch();
    for (const token of invalidTokens) {
      const tokenDocs = await userRef
        .collection(COLLECTIONS.fcmTokens)
        .where("token", "==", token)
        .get();
      tokenDocs.docs.forEach((doc) => batch.update(doc.ref, { enabled: false }));
    }
    await batch.commit();
  }
}

function toRadians(deg: number): number {
  return (deg * Math.PI) / 180;
}

function distanceKm(aLat: number, aLng: number, bLat: number, bLng: number): number {
  const earth = 6371;
  const dLat = toRadians(bLat - aLat);
  const dLng = toRadians(bLng - aLng);
  const aa =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRadians(aLat)) * Math.cos(toRadians(bLat)) * Math.sin(dLng / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(aa), Math.sqrt(1 - aa));
  return earth * c;
}

export const onCommentCreated = onDocumentCreated(
  `${COLLECTIONS.comments}/{commentId}`,
  async (event) => {
    const comment = event.data?.data();
    if (!comment) return;

    const postId = String(comment.postId ?? "");
    const authorId = String(comment.authorId ?? "");
    if (!postId) return;

    const postSnap = await db.collection(COLLECTIONS.posts).doc(postId).get();
    if (!postSnap.exists) return;

    const post = postSnap.data() ?? {};
    const postOwnerId = String(post.authorId ?? "");
    if (!postOwnerId || postOwnerId === authorId) return;

    const title = "Nuevo comentario en tu publicación";
    const body = String(comment.content ?? "Alguien comentó en tu publicación.").slice(0, 120);

    const notificationId = await createNotification({
      userId: postOwnerId,
      type: "NEW_COMMENT",
      title,
      body,
      relatedPostId: postId,
      metadata: {
        commentId: event.params.commentId,
      },
    });

    await sendPushIfEnabled({
      userId: postOwnerId,
      title,
      body,
      relatedPostId: postId,
      notificationId,
    });
  }
);

export const onPostModerationChanged = onDocumentUpdated(
  `${COLLECTIONS.posts}/{postId}`,
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after) return;

    const prevStatus = String(before.status ?? "");
    const newStatus = String(after.status ?? "");

    if (prevStatus === newStatus) return;
    if (newStatus !== "VERIFIED" && newStatus !== "REJECTED") return;

    const userId = String(after.authorId ?? "");
    const postId = String(event.params.postId ?? "");
    if (!userId || !postId) return;

    const approved = newStatus === "VERIFIED";
    const title = approved ? "Tu publicación fue aprobada" : "Tu publicación fue rechazada";
    const body = approved
      ? "Tu publicación ya es visible para la comunidad."
      : String(after.rejectionReason ?? "Revisa los detalles para volver a publicar.");

    const notificationId = await createNotification({
      userId,
      type: approved ? "POST_APPROVED" : "POST_REJECTED",
      title,
      body,
      relatedPostId: postId,
      metadata: {
        status: newStatus,
      },
    });

    await sendPushIfEnabled({
      userId,
      title,
      body,
      relatedPostId: postId,
      notificationId,
    });
  }
);

export const onNearbyPostCreated = onDocumentCreated(
  `${COLLECTIONS.posts}/{postId}`,
  async (event) => {
    const post = event.data?.data();
    if (!post) return;

    const postId = String(event.params.postId ?? "");
    const postAuthorId = String(post.authorId ?? "");
    const postTitle = String(post.title ?? "Nueva publicación");

    const postLat = Number(post.latitude ?? 0);
    const postLng = Number(post.longitude ?? 0);
    if (!postId || !Number.isFinite(postLat) || !Number.isFinite(postLng) || postLat === 0 || postLng === 0) {
      return;
    }

    const usersSnap = await db
      .collection(COLLECTIONS.users)
      .where("alertsNearMe", "==", true)
      .get();

    for (const userDoc of usersSnap.docs) {
      if (userDoc.id === postAuthorId) continue;
      const user = userDoc.data();

      const userLat = Number(user.latitude ?? user.lastKnownLatitude ?? 0);
      const userLng = Number(user.longitude ?? user.lastKnownLongitude ?? 0);
      if (!Number.isFinite(userLat) || !Number.isFinite(userLng) || userLat === 0 || userLng === 0) {
        continue;
      }

      const radiusKm = Number(user.notificationRadiusKm ?? 10);
      const km = distanceKm(postLat, postLng, userLat, userLng);
      if (km > radiusKm) continue;

      const title = "Nueva publicación cerca de ti";
      const body = `${postTitle} está aproximadamente a ${km.toFixed(1)} km.`;

      const notificationId = await createNotification({
        userId: userDoc.id,
        type: "NEW_POST_NEARBY",
        title,
        body,
        relatedPostId: postId,
        metadata: {
          distanceKm: Number(km.toFixed(2)),
          radiusKm,
        },
      });

      await sendPushIfEnabled({
        userId: userDoc.id,
        title,
        body,
        relatedPostId: postId,
        notificationId,
      });
    }
  }
);

// HTTP proxy to call OpenRouter securely from the client.
// Reads OPENROUTER_API_KEY from environment (do NOT commit your key).
export const openRouterProxy = https.onRequest(async (req, res) => {
  try {
    const apiKey = process.env.OPENROUTER_API_KEY;
    if (!apiKey) {
      res.status(500).json({ error: "OPENROUTER_API_KEY not configured" });
      return;
    }

    const body = req.body ?? {};
    const model = body.model ?? "google/gemma-4-31b-it:free";
    const messages = body.messages ?? [];
    const reasoning = body.reasoning ?? { enabled: true };

    const payload = { model, messages, reasoning };

    const r = await (globalThis as any).fetch("https://openrouter.ai/api/v1/chat/completions", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    const json = await r.json();
    res.status(r.status).json(json);
  } catch (err: any) {
    logger.error("openRouterProxy error", err);
    res.status(500).json({ error: err?.message ?? String(err) });
  }
});
