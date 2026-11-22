package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class DiajakRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google terlebih dahulu.")

    private fun <T> Flow<T>.logFirestoreErrors(uid: String?, operationType: OperationType, path: String): Flow<T> =
        this.catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val exceptionToLog = firestoreError ?: (error as? Exception)
            if (exceptionToLog != null && (uid == null || auth.currentUser?.uid == uid)) {
                handleFirestoreError(exceptionToLog, operationType, path)
            }
            throw error
        }

    // --- REALTIME OBSERVATION STREAMS ---

    fun observeUserProfile(uid: String): Flow<UserProfile?> =
        db.collection("users").document(uid)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .logFirestoreErrors(uid, OperationType.GET, "users/$uid")

    fun observeKycDocument(uid: String): Flow<KycDocument?> =
        db.collection("users").document(uid).collection("kycDocuments").document("ktp")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(KycDocument::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .logFirestoreErrors(uid, OperationType.GET, "users/$uid/kycDocuments/ktp")

    fun observePublishedActivities(uid: String?): Flow<List<ActivityItem>> =
        db.collection("activities")
            .whereEqualTo("status", "PUBLISHED")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ActivityItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .logFirestoreErrors(uid, OperationType.LIST, "activities")

    fun observeOrganizerActivities(uid: String): Flow<List<ActivityItem>> =
        db.collection("activities")
            .whereEqualTo("organizerId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ActivityItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.eventDateMillis }
            }
            .logFirestoreErrors(uid, OperationType.LIST, "activities")

    fun observeFavorites(uid: String): Flow<Set<String>> =
        db.collection("users").document(uid).collection("favorites")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FavoriteItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .map { it.activityId.ifBlank { it.id } }
                    .toSet()
            }
            .logFirestoreErrors(uid, OperationType.LIST, "users/$uid/favorites")

    fun observeNotifications(uid: String): Flow<List<AppNotification>> =
        db.collection("users").document(uid).collection("notifications")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .logFirestoreErrors(uid, OperationType.LIST, "users/$uid/notifications")

    fun observeWalletTransactions(uid: String): Flow<List<WalletTransaction>> =
        db.collection("users").document(uid).collection("walletTransactions")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(WalletTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .logFirestoreErrors(uid, OperationType.LIST, "users/$uid/walletTransactions")

    fun observeParticipantBookings(uid: String): Flow<List<BookingTicket>> =
        db.collection("bookings")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(BookingTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .logFirestoreErrors(uid, OperationType.LIST, "bookings")

    fun observeOrganizerBookings(uid: String): Flow<List<BookingTicket>> =
        db.collection("bookings")
            .whereEqualTo("organizerId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(BookingTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .logFirestoreErrors(uid, OperationType.LIST, "bookings")

    fun observeConversations(uid: String): Flow<List<Conversation>> =
        db.collection("conversations")
            .whereArrayContains("participantUids", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.updatedAt?.seconds ?: 0L }
            }
            .logFirestoreErrors(uid, OperationType.LIST, "conversations")

    fun observeMessages(conversationId: String, uid: String): Flow<List<ChatMessage>> =
        db.collection("conversations").document(conversationId).collection("messages")
            .whereArrayContains("participantUids", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }
            .logFirestoreErrors(uid, OperationType.LIST, "conversations/$conversationId/messages")

    fun observeActivityReviews(activityId: String, uid: String?): Flow<List<ActivityReview>> =
        db.collection("activities").document(activityId).collection("reviews")
            .whereEqualTo("activityId", activityId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ActivityReview::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .logFirestoreErrors(uid, OperationType.LIST, "activities/$activityId/reviews")

    // --- BOOTSTRAP & PROFILE ---

    suspend fun ensureUserProfileAndSeedCatalog(user: FirebaseUser): Result<Unit> {
        val uid = user.uid
        return try {
            val userRef = db.collection("users").document(uid)
            val existingUser = userRef.get().await()
            if (!existingUser.exists()) {
                val safeName = (user.displayName ?: "Pengguna Diajak").trim().take(80).ifBlank { "Pengguna Diajak" }
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "bio" to "Eksplor aktivitas seru dan komunitas baru di diajak.com",
                    "city" to "Jakarta Selatan",
                    "photoUrl" to (user.photoUrl?.toString()?.take(500) ?: ""),
                    "interests" to listOf("Lari", "Kebugaran", "Fotografi"),
                    "organizerVerified" to false,
                    "kycStatus" to "UNVERIFIED",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(payload).await()
                createNotificationInternal(
                    targetUid = uid,
                    title = "Selamat datang di diajak!",
                    body = "Temukan aktivitas terdekat atau verifikasi KTP di Portal Penyelenggara untuk membuat acaramu sendiri.",
                    type = "SYSTEM",
                    relatedId = ""
                )
            }

            // Seed showcase upcoming activities if marketplace has no published activities yet
            val existingPublished = db.collection("activities")
                .whereEqualTo("status", "PUBLISHED")
                .limit(1)
                .get()
                .await()
            if (existingPublished.isEmpty) {
                val batch = db.batch()
                DiajakCatalog.buildShowcaseSeedActivities().forEach { (docId, baseMap) ->
                    val ref = db.collection("activities").document(docId)
                    val fullMap = baseMap.toMutableMap().apply {
                        put("createdAt", FieldValue.serverTimestamp())
                        put("updatedAt", FieldValue.serverTimestamp())
                    }
                    batch.set(ref, fullMap)
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        displayName: String,
        bio: String,
        city: String,
        photoUrl: String,
        interests: List<String>
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val path = "users/$uid"
        return try {
            val cleanName = displayName.trim().take(80)
            require(cleanName.isNotEmpty()) { "Nama tampilan tidak boleh kosong." }
            db.collection("users").document(uid).update(
                mapOf(
                    "displayName" to cleanName,
                    "bio" to bio.trim().take(240),
                    "city" to city.trim().take(60).ifBlank { "Jakarta Selatan" },
                    "photoUrl" to photoUrl.take(500),
                    "interests" to interests.take(15),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updatePreferredCity(city: String): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val path = "users/$uid"
        return try {
            db.collection("users").document(uid).update(
                mapOf(
                    "city" to city.trim().take(60).ifBlank { "Jakarta Selatan" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- ORGANIZER KYC VERIFICATION ---

    suspend fun submitKycDocument(
        fullName: String,
        nikNumber: String,
        documentPhotoUri: String
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val path = "users/$uid/kycDocuments/ktp"
        return try {
            val cleanName = fullName.trim().take(100)
            val cleanNik = nikNumber.filter { it.isDigit() }
            require(cleanName.length >= 2) { "Nama sesuai KTP minimal 2 karakter." }
            require(cleanNik.length == 16) { "NIK harus tepat 16 digit angka." }
            require(documentPhotoUri.isNotBlank()) { "Unggah foto KTP terlebih dahulu." }

            val batch = db.batch()
            val kycRef = db.collection("users").document(uid).collection("kycDocuments").document("ktp")
            val userRef = db.collection("users").document(uid)

            batch.set(
                kycRef,
                mapOf(
                    "userId" to uid,
                    "fullName" to cleanName,
                    "nikNumber" to cleanNik,
                    "documentPhotoUri" to documentPhotoUri.take(1000),
                    "status" to "VERIFIED",
                    "submittedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                userRef,
                mapOf(
                    "organizerVerified" to true,
                    "kycStatus" to "VERIFIED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            createNotificationInternal(
                targetUid = uid,
                title = "Dokumen KTP Terverifikasi",
                body = "Selamat! Akun penyelenggara atas nama $cleanName telah terverifikasi. Kamu sekarang dapat menerbitkan aktivitas ke publik.",
                type = "KYC",
                relatedId = "ktp"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- FAVORITES ---

    suspend fun toggleFavorite(activity: ActivityItem, currentlyFavorited: Boolean): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val favPath = "users/$uid/favorites/${activity.id}"
        return try {
            val favRef = db.collection("users").document(uid).collection("favorites").document(activity.id)
            val actRef = db.collection("activities").document(activity.id)
            if (currentlyFavorited) {
                favRef.delete().await()
                val newFavCount = (activity.favoriteCount - 1L).coerceAtLeast(0L)
                actRef.update(
                    mapOf(
                        "favoriteCount" to newFavCount,
                        "viralScore" to (activity.viralScore - 5L).coerceAtLeast(0L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                favRef.set(
                    mapOf(
                        "activityId" to activity.id,
                        "savedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                actRef.update(
                    mapOf(
                        "favoriteCount" to (activity.favoriteCount + 1L),
                        "viralScore" to (activity.viralScore + 5L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, favPath)
            Result.failure(e)
        }
    }

    // --- ORGANIZER ACTIVITIES CRUD ---

    suspend fun saveActivity(
        existingId: String?,
        organizerProfile: UserProfile,
        title: String,
        description: String,
        niche: NicheCategory,
        cityOption: CityOption,
        locationName: String,
        eventDateMillis: Long,
        timeLabel: String,
        priceIdr: Long,
        quota: Long,
        photoKeys: List<String>,
        benefits: List<String>,
        publishNow: Boolean
    ): Result<String> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanTitle = title.trim().take(120)
        val cleanDesc = description.trim().take(2000)
        val cleanLoc = locationName.trim().take(150)
        val cleanPhotos = photoKeys.filter { it.isNotBlank() }.take(6).ifEmpty { listOf("img_activity_running") }
        val cleanBenefits = benefits.map { it.trim() }.filter { it.isNotBlank() }.take(12)
            .ifEmpty { listOf("Akses penuh ke aktivitas & grup peserta") }

        return try {
            require(cleanTitle.length >= 3) { "Judul aktivitas minimal 3 karakter." }
            require(cleanDesc.length >= 10) { "Deskripsi aktivitas minimal 10 karakter." }
            require(cleanLoc.length >= 2) { "Lokasi aktivitas wajib diisi." }
            require(quota in 1..10000) { "Kuota peserta harus antara 1 sampai 10.000." }
            require(priceIdr in 0..100_000_000) { "Harga tidak valid." }

            if (publishNow && !organizerProfile.organizerVerified) {
                return Result.failure(IllegalStateException("Verifikasi dokumen KTP di Kelola Dokumen terlebih dahulu sebelum menerbitkan aktivitas."))
            }
            val targetStatus = if (publishNow) "PUBLISHED" else "DRAFT"

            if (existingId.isNullOrBlank()) {
                val docRef = db.collection("activities").document()
                val payload = mapOf(
                    "organizerId" to uid,
                    "organizerName" to organizerProfile.displayName.ifBlank { "Penyelenggara Diajak" }.take(80),
                    "organizerAvatarKey" to organizerProfile.photoUrl.take(500),
                    "organizerVerified" to organizerProfile.organizerVerified,
                    "title" to cleanTitle,
                    "description" to cleanDesc,
                    "category" to niche.name,
                    "categoryEmoji" to niche.emoji,
                    "city" to cityOption.name,
                    "locationName" to cleanLoc,
                    "latitude" to cityOption.latitude,
                    "longitude" to cityOption.longitude,
                    "eventDateMillis" to eventDateMillis,
                    "timeLabel" to timeLabel.trim().take(40).ifBlank { "08:00 WIB" },
                    "priceIdr" to priceIdr,
                    "quota" to quota,
                    "joinedCount" to 0L,
                    "favoriteCount" to 0L,
                    "viralScore" to 50L,
                    "photoKeys" to cleanPhotos,
                    "benefits" to cleanBenefits,
                    "joinedUserNames" to emptyList<String>(),
                    "status" to targetStatus,
                    "averageRating" to 0.0,
                    "reviewCount" to 0L,
                    "seededSample" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                Result.success(docRef.id)
            } else {
                val docRef = db.collection("activities").document(existingId)
                docRef.update(
                    mapOf(
                        "title" to cleanTitle,
                        "description" to cleanDesc,
                        "category" to niche.name,
                        "categoryEmoji" to niche.emoji,
                        "city" to cityOption.name,
                        "locationName" to cleanLoc,
                        "latitude" to cityOption.latitude,
                        "longitude" to cityOption.longitude,
                        "eventDateMillis" to eventDateMillis,
                        "timeLabel" to timeLabel.trim().take(40).ifBlank { "08:00 WIB" },
                        "priceIdr" to priceIdr,
                        "quota" to quota,
                        "photoKeys" to cleanPhotos,
                        "benefits" to cleanBenefits,
                        "status" to targetStatus,
                        "organizerVerified" to organizerProfile.organizerVerified,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(existingId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "activities/${existingId ?: "new"}")
            Result.failure(e)
        }
    }

    suspend fun updateActivityStatus(
        activityId: String,
        newStatus: String,
        organizerVerified: Boolean
    ): Result<Unit> {
        try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            if (newStatus == "PUBLISHED" && !organizerVerified) {
                return Result.failure(IllegalStateException("Unggah & verifikasi KTP di Kelola Dokumen untuk menerbitkan aktivitas."))
            }
            db.collection("activities").document(activityId).update(
                mapOf(
                    "status" to newStatus,
                    "organizerVerified" to organizerVerified,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "activities/$activityId")
            Result.failure(e)
        }
    }

    // --- BOOKING & CHECKOUT ---

    suspend fun completeBooking(
        activity: ActivityItem,
        userProfile: UserProfile,
        ticketCount: Int,
        paymentMethodCode: String
    ): Result<BookingTicket> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val count = ticketCount.coerceIn(1, 10)
        val totalPrice = activity.priceIdr * count
        val method = if (totalPrice == 0L) "GRATIS" else paymentMethodCode
        val ticketCode = "DJK-${Random.nextInt(100000, 999999)}"
        val participantName = userProfile.displayName.ifBlank { "Peserta Diajak" }.take(80)

        return try {
            val bookingRef = db.collection("bookings").document()
            val bookingPayload = mapOf(
                "activityId" to activity.id,
                "activityTitle" to activity.title.take(120),
                "activityPhotoKey" to (activity.photoKeys.firstOrNull() ?: "img_activity_running").take(500),
                "activityLocation" to activity.locationName.take(150),
                "activityDateMillis" to activity.eventDateMillis,
                "activityTimeLabel" to activity.timeLabel.take(40),
                "userId" to uid,
                "userName" to participantName,
                "organizerId" to activity.organizerId,
                "organizerName" to activity.organizerName.take(80),
                "ticketCount" to count.toLong(),
                "totalPriceIdr" to totalPrice,
                "paymentMethod" to method,
                "ticketCode" to ticketCode,
                "status" to "CONFIRMED",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            bookingRef.set(bookingPayload).await()

            // Increment activity joinedCount & add user name to preview list
            val updatedNames = (listOf(participantName) + activity.joinedUserNames).distinct().take(20)
            db.collection("activities").document(activity.id).update(
                mapOf(
                    "joinedCount" to (activity.joinedCount + count),
                    "joinedUserNames" to updatedNames,
                    "viralScore" to (activity.viralScore + 15L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Join or create Activity Group Chat automatically
            joinOrCreateActivityGroupChat(activity, uid, participantName)

            // Credit organizer wallet ledger if paid
            if (totalPrice > 0L && activity.organizerId.isNotBlank()) {
                val txRef = db.collection("users").document(activity.organizerId)
                    .collection("walletTransactions").document()
                txRef.set(
                    mapOf(
                        "userId" to activity.organizerId,
                        "type" to "INCOME",
                        "amount" to totalPrice,
                        "title" to "Tiket ${activity.title.take(60)} ($participantName)",
                        "referenceId" to bookingRef.id,
                        "status" to "SUCCESS",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Notifications for participant & organizer
            createNotificationInternal(
                targetUid = uid,
                title = "E-Tiket Terbit: ${activity.title.take(50)}",
                body = "Booking $count tiket berhasil ($method). Kode tiket: $ticketCode. Kamu otomatis masuk ke Grup Chat aktivitas.",
                type = "BOOKING",
                relatedId = bookingRef.id
            )
            if (activity.organizerId.isNotBlank() && activity.organizerId != uid) {
                createNotificationInternal(
                    targetUid = activity.organizerId,
                    title = "Pendaftar Baru!",
                    body = "$participantName memesan $count tiket untuk ${activity.title.take(60)}.",
                    type = "BOOKING",
                    relatedId = bookingRef.id
                )
            }

            val created = BookingTicket(
                id = bookingRef.id,
                activityId = activity.id,
                activityTitle = activity.title,
                activityPhotoKey = activity.photoKeys.firstOrNull() ?: "img_activity_running",
                activityLocation = activity.locationName,
                activityDateMillis = activity.eventDateMillis,
                activityTimeLabel = activity.timeLabel,
                userId = uid,
                userName = participantName,
                organizerId = activity.organizerId,
                organizerName = activity.organizerName,
                ticketCount = count.toLong(),
                totalPriceIdr = totalPrice,
                paymentMethod = method,
                ticketCode = ticketCode,
                status = "CONFIRMED"
            )
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "bookings")
            Result.failure(e)
        }
    }

    suspend fun checkInBooking(bookingId: String): Result<Unit> {
        try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            db.collection("bookings").document(bookingId).update(
                mapOf(
                    "status" to "CHECKED_IN",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "bookings/$bookingId")
            Result.failure(e)
        }
    }

    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            db.collection("bookings").document(bookingId).update(
                mapOf(
                    "status" to "CANCELLED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "bookings/$bookingId")
            Result.failure(e)
        }
    }

    // --- CONVERSATIONS & CHAT ---

    private suspend fun joinOrCreateActivityGroupChat(
        activity: ActivityItem,
        uid: String,
        participantName: String
    ) {
        val convId = "group_${activity.id}".take(128)
        val convRef = db.collection("conversations").document(convId)
        val welcomeMsg = "$participantName bergabung ke aktivitas ${activity.title.take(60)}"
        val organizerId = activity.organizerId.ifBlank { uid }
        val initialParticipants = listOf(uid, organizerId).distinct()

        try {
            val snap = convRef.get().await()
            if (snap.exists()) {
                val existingParticipants = (snap.get("participantUids") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList()
                val merged = (existingParticipants + uid + organizerId).distinct().take(100)
                val updatePayload = if (merged.size != existingParticipants.size || !existingParticipants.containsAll(merged)) {
                    mapOf(
                        "participantUids" to merged,
                        "lastMessage" to welcomeMsg.take(500),
                        "lastMessageSenderName" to participantName,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                } else {
                    mapOf(
                        "lastMessage" to welcomeMsg.take(500),
                        "lastMessageSenderName" to participantName,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
                convRef.update(updatePayload).await()
                convRef.collection("messages").document().set(
                    mapOf(
                        "conversationId" to convId,
                        "senderId" to uid,
                        "senderName" to participantName,
                        "text" to "Halo semuanya! Saya baru saja memesan tiket untuk ${activity.title.take(80)}.",
                        "participantUids" to merged,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                convRef.set(
                    mapOf(
                        "type" to "GROUP",
                        "activityId" to activity.id,
                        "activityTitle" to activity.title.take(120),
                        "title" to "Grup: ${activity.title}".take(120),
                        "organizerId" to organizerId,
                        "participantUids" to initialParticipants,
                        "lastMessage" to welcomeMsg.take(500),
                        "lastMessageSenderName" to participantName,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                convRef.collection("messages").document().set(
                    mapOf(
                        "conversationId" to convId,
                        "senderId" to uid,
                        "senderName" to participantName,
                        "text" to "Halo semuanya! Saya baru saja memesan tiket untuk ${activity.title.take(80)}.",
                        "participantUids" to initialParticipants,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // If conversation exists but user isn't listed yet, get() throws PERMISSION_DENIED;
            // fall back directly to GROUP update using arrayUnion, then fetch merged participant list.
            try {
                convRef.update(
                    mapOf(
                        "participantUids" to FieldValue.arrayUnion(uid),
                        "lastMessage" to welcomeMsg.take(500),
                        "lastMessageSenderName" to participantName,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                val joinedParticipants = try {
                    val refreshed = convRef.get().await()
                    (refreshed.get("participantUids") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.takeIf { it.contains(uid) }
                        ?: initialParticipants
                } catch (_: Exception) {
                    initialParticipants
                }
                convRef.collection("messages").document().set(
                    mapOf(
                        "conversationId" to convId,
                        "senderId" to uid,
                        "senderName" to participantName,
                        "text" to "Halo semuanya! Saya baru saja bergabung di ${activity.title.take(80)}.",
                        "participantUids" to joinedParticipants,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (inner: CancellationException) {
                throw inner
            } catch (inner: Exception) {
                handleFirestoreError(inner, OperationType.WRITE, "conversations/$convId")
            }
        }
    }

    suspend fun openOrCreateDirectChat(
        userName: String,
        activity: ActivityItem
    ): Result<String> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val sortedPair = listOf(uid, activity.organizerId).distinct().sorted()
        val rawId = "direct_${sortedPair.joinToString("_")}_${activity.id}"
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(120)
        return try {
            val convRef = db.collection("conversations").document(rawId)
            val existing = try {
                convRef.get().await()
            } catch (_: Exception) {
                null
            }
            if (existing == null || !existing.exists()) {
                val participants = listOf(uid, activity.organizerId).distinct()
                convRef.set(
                    mapOf(
                        "type" to "DIRECT",
                        "activityId" to activity.id,
                        "activityTitle" to activity.title.take(120),
                        "title" to activity.organizerName.take(120),
                        "organizerId" to activity.organizerId,
                        "participantUids" to participants,
                        "lastMessage" to "Memulai percakapan tentang ${activity.title.take(80)}",
                        "lastMessageSenderName" to userName.take(80),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(rawId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "conversations/$rawId")
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        conversation: Conversation,
        senderName: String,
        text: String
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanText = text.trim().take(1000)
        if (cleanText.isEmpty()) return Result.failure(IllegalArgumentException("Pesan tidak boleh kosong."))
        val path = "conversations/${conversation.id}/messages"
        return try {
            val participants = (conversation.participantUids + uid).distinct().take(100)
            val convRef = db.collection("conversations").document(conversation.id)
            val msgRef = convRef.collection("messages").document()
            msgRef.set(
                mapOf(
                    "conversationId" to conversation.id,
                    "senderId" to uid,
                    "senderName" to senderName.ifBlank { "Pengguna" }.take(80),
                    "text" to cleanText,
                    "participantUids" to participants,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            convRef.update(
                mapOf(
                    "lastMessage" to cleanText.take(500),
                    "lastMessageSenderName" to senderName.ifBlank { "Pengguna" }.take(80),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            // Notify other participant in direct chat
            val otherUid = participants.firstOrNull { it != uid }
            if (conversation.type == "DIRECT" && !otherUid.isNullOrBlank() && !otherUid.startsWith("community_")) {
                createNotificationInternal(
                    targetUid = otherUid,
                    title = "Pesan baru dari ${senderName.take(40)}",
                    body = cleanText.take(160),
                    type = "MESSAGE",
                    relatedId = conversation.id
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- REVIEWS & ORGANIZER REPLIES ---

    suspend fun addReview(
        activity: ActivityItem,
        authorName: String,
        rating: Int,
        comment: String
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanComment = comment.trim().take(600)
        val safeRating = rating.coerceIn(1, 5)
        val path = "activities/${activity.id}/reviews"
        return try {
            require(cleanComment.length >= 2) { "Tulis ulasan minimal 2 karakter." }
            val reviewRef = db.collection("activities").document(activity.id)
                .collection("reviews").document()
            reviewRef.set(
                mapOf(
                    "activityId" to activity.id,
                    "organizerId" to activity.organizerId,
                    "authorId" to uid,
                    "authorName" to authorName.ifBlank { "Peserta" }.take(80),
                    "rating" to safeRating.toLong(),
                    "comment" to cleanComment,
                    "organizerReply" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val newCount = activity.reviewCount + 1L
            val newAvg = (((activity.averageRating * activity.reviewCount) + safeRating) / newCount.toDouble())
                .coerceIn(0.0, 5.0)
            db.collection("activities").document(activity.id).update(
                mapOf(
                    "averageRating" to newAvg,
                    "reviewCount" to newCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun replyToReview(
        activityId: String,
        reviewId: String,
        replyText: String
    ): Result<Unit> {
        try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanReply = replyText.trim().take(600)
        val path = "activities/$activityId/reviews/$reviewId"
        return try {
            require(cleanReply.isNotEmpty()) { "Balasan ulasan tidak boleh kosong." }
            db.collection("activities").document(activityId)
                .collection("reviews").document(reviewId)
                .update(
                    mapOf(
                        "organizerReply" to cleanReply,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- WALLET & NOTIFICATIONS ---

    suspend fun requestWithdrawal(
        amount: Long,
        destinationLabel: String,
        availableBalance: Long
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val path = "users/$uid/walletTransactions"
        return try {
            require(amount >= 10_000L) { "Minimal penarikan saldo adalah Rp 10.000." }
            require(amount <= availableBalance) { "Saldo tersedia tidak mencukupi." }
            val cleanDest = destinationLabel.trim().take(100)
            require(cleanDest.length >= 3) { "Masukkan tujuan rekening / e-wallet yang valid." }

            val txRef = db.collection("users").document(uid).collection("walletTransactions").document()
            txRef.set(
                mapOf(
                    "userId" to uid,
                    "type" to "WITHDRAWAL",
                    "amount" to amount,
                    "title" to "Penarikan ke $cleanDest".take(150),
                    "referenceId" to "WD-${Random.nextInt(10000, 99999)}",
                    "status" to "SUCCESS",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            createNotificationInternal(
                targetUid = uid,
                title = "Penarikan Saldo Berhasil",
                body = "Dana sebesar ${DiajakCatalog.formatRupiah(amount)} telah diproses ke $cleanDest.",
                type = "WALLET",
                relatedId = txRef.id
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(notifId: String): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val path = "users/$uid/notifications/$notifId"
        return try {
            db.collection("users").document(uid)
                .collection("notifications").document(notifId)
                .update(mapOf("readState" to true))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    private suspend fun createNotificationInternal(
        targetUid: String,
        title: String,
        body: String,
        type: String,
        relatedId: String
    ) {
        try {
            db.collection("users").document(targetUid).collection("notifications").document().set(
                mapOf(
                    "title" to title.take(120),
                    "body" to body.take(400),
                    "type" to type,
                    "relatedId" to relatedId.take(128),
                    "readState" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
            // Non-blocking notification helper
        }
    }
}
