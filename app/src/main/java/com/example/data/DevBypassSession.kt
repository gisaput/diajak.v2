package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.random.Random

/**
 * TEMPORARY DEVELOPMENT-ONLY LOGIN BYPASS.
 * Set [ENABLE_DEV_LOGIN_BYPASS] to false or delete this file and its usages before production release.
 *
 * Uses a local in-memory [DiajakDataSource] so every authenticated screen and flow can be tested
 * in the emulator without modifying Firebase Auth or weakening Firestore security rules.
 */
object DevBypassSession {
    const val ENABLE_DEV_LOGIN_BYPASS = true
    const val DEV_USER_ID = "dev_bypass_user"
    const val DEV_USER_LABEL = "Mode Dev Bypass (Sesi Lokal)"

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    @Volatile
    private var localDataSource: DevBypassDataSource? = null

    fun activate() {
        if (!ENABLE_DEV_LOGIN_BYPASS) return
        if (localDataSource == null) {
            localDataSource = DevBypassDataSource()
        }
        _isActive.value = true
    }

    fun deactivate() {
        _isActive.value = false
        localDataSource = null
    }

    fun isDevUserId(uid: String?): Boolean =
        ENABLE_DEV_LOGIN_BYPASS && uid == DEV_USER_ID

    fun dataSource(): DiajakDataSource {
        return localDataSource ?: DevBypassDataSource().also { localDataSource = it }
    }
}

private class DevBypassDataSource : DiajakDataSource {
    private val uid = DevBypassSession.DEV_USER_ID

    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = uid,
            userId = uid,
            displayName = "Developer Diajak",
            bio = "Akun bypass sementara untuk pengujian cepat UI & alur aplikasi.",
            city = "Jakarta Selatan",
            photoUrl = "",
            interests = listOf("Lari", "Padel", "Fotografi"),
            organizerVerified = false,
            kycStatus = "UNVERIFIED",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    )

    private val _kycDocument = MutableStateFlow<KycDocument?>(null)

    private val _activities = MutableStateFlow(DiajakCatalog.buildShowcaseActivityItems())

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())

    private val _notifications = MutableStateFlow(
        listOf(
            AppNotification(
                id = "dev_welcome_notif",
                title = "Mode Dev Bypass Aktif",
                body = "Kamu masuk tanpa login Google untuk pengujian lokal. Data di sesi ini tersimpan di memori sementara dan tidak mengubah database cloud.",
                type = "SYSTEM",
                relatedId = "",
                readState = false,
                createdAt = Timestamp.now()
            )
        )
    )

    private val _walletTransactions = MutableStateFlow<List<WalletTransaction>>(emptyList())

    private val _bookings = MutableStateFlow<List<BookingTicket>>(emptyList())

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    private val _reviews = MutableStateFlow<Map<String, List<ActivityReview>>>(emptyMap())

    override fun observeUserProfile(uid: String): Flow<UserProfile?> = _userProfile

    override fun observeKycDocument(uid: String): Flow<KycDocument?> = _kycDocument

    override fun observePublishedActivities(uid: String?): Flow<List<ActivityItem>> =
        _activities.map { list -> list.filter { it.status == "PUBLISHED" } }

    override fun observeOrganizerActivities(uid: String): Flow<List<ActivityItem>> =
        _activities.map { list ->
            list.filter { it.organizerId == uid }.sortedByDescending { it.eventDateMillis }
        }

    override fun observeFavorites(uid: String): Flow<Set<String>> = _favoriteIds

    override fun observeNotifications(uid: String): Flow<List<AppNotification>> =
        _notifications.map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }

    override fun observeWalletTransactions(uid: String): Flow<List<WalletTransaction>> =
        _walletTransactions.map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }

    override fun observeParticipantBookings(uid: String): Flow<List<BookingTicket>> =
        _bookings.map { list ->
            list.filter { it.userId == uid }.sortedByDescending { it.createdAt?.seconds ?: 0L }
        }

    override fun observeOrganizerBookings(uid: String): Flow<List<BookingTicket>> =
        _bookings.map { list ->
            list.filter { it.organizerId == uid }.sortedByDescending { it.createdAt?.seconds ?: 0L }
        }

    override fun observeConversations(uid: String): Flow<List<Conversation>> =
        _conversations.map { list ->
            list.filter { uid in it.participantUids }
                .sortedByDescending { it.updatedAt?.seconds ?: 0L }
        }

    override fun observeMessages(conversationId: String, uid: String): Flow<List<ChatMessage>> =
        _messages.map { map ->
            map[conversationId].orEmpty().sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
        }

    override fun observeActivityReviews(activityId: String, uid: String?): Flow<List<ActivityReview>> =
        _reviews.map { map ->
            map[activityId].orEmpty().sortedByDescending { it.createdAt?.seconds ?: 0L }
        }

    override suspend fun ensureUserProfileAndSeedCatalog(user: FirebaseUser): Result<Unit> =
        Result.success(Unit)

    override suspend fun updateUserProfile(
        displayName: String,
        bio: String,
        city: String,
        photoUrl: String,
        interests: List<String>
    ): Result<Unit> {
        val cleanName = displayName.trim().take(80)
        if (cleanName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Nama tampilan tidak boleh kosong."))
        }
        _userProfile.update { current ->
            current.copy(
                displayName = cleanName,
                bio = bio.trim().take(240),
                city = city.trim().take(60).ifBlank { "Jakarta Selatan" },
                photoUrl = photoUrl.take(500),
                interests = interests.take(15),
                updatedAt = Timestamp.now()
            )
        }
        return Result.success(Unit)
    }

    override suspend fun updatePreferredCity(city: String): Result<Unit> {
        _userProfile.update { current ->
            current.copy(
                city = city.trim().take(60).ifBlank { "Jakarta Selatan" },
                updatedAt = Timestamp.now()
            )
        }
        return Result.success(Unit)
    }

    override suspend fun submitKycDocument(
        fullName: String,
        nikNumber: String,
        documentPhotoUri: String
    ): Result<Unit> {
        val cleanName = fullName.trim().take(100)
        val cleanNik = nikNumber.filter { it.isDigit() }
        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("Nama sesuai KTP minimal 2 karakter."))
        }
        if (cleanNik.length != 16) {
            return Result.failure(IllegalArgumentException("NIK harus tepat 16 digit angka."))
        }
        if (documentPhotoUri.isBlank()) {
            return Result.failure(IllegalArgumentException("Unggah foto KTP terlebih dahulu."))
        }
        val now = Timestamp.now()
        _kycDocument.value = KycDocument(
            id = "ktp",
            userId = uid,
            fullName = cleanName,
            nikNumber = cleanNik,
            documentPhotoUri = documentPhotoUri.take(1000),
            status = "VERIFIED",
            submittedAt = now
        )
        _userProfile.update { current ->
            current.copy(
                organizerVerified = true,
                kycStatus = "VERIFIED",
                updatedAt = now
            )
        }
        pushNotification(
            title = "Dokumen KTP Terverifikasi",
            body = "Selamat! Akun penyelenggara atas nama $cleanName telah terverifikasi di sesi Dev Bypass.",
            type = "KYC",
            relatedId = "ktp"
        )
        return Result.success(Unit)
    }

    override suspend fun toggleFavorite(activity: ActivityItem, currentlyFavorited: Boolean): Result<Unit> {
        _favoriteIds.update { current ->
            if (currentlyFavorited) current - activity.id else current + activity.id
        }
        _activities.update { list ->
            list.map { item ->
                if (item.id != activity.id) {
                    item
                } else if (currentlyFavorited) {
                    item.copy(
                        favoriteCount = (item.favoriteCount - 1L).coerceAtLeast(0L),
                        viralScore = (item.viralScore - 5L).coerceAtLeast(0L),
                        updatedAt = Timestamp.now()
                    )
                } else {
                    item.copy(
                        favoriteCount = item.favoriteCount + 1L,
                        viralScore = item.viralScore + 5L,
                        updatedAt = Timestamp.now()
                    )
                }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun saveActivity(
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
        val cleanTitle = title.trim().take(120)
        val cleanDesc = description.trim().take(2000)
        val cleanLoc = locationName.trim().take(150)
        val cleanPhotos = photoKeys.filter { it.isNotBlank() }.take(6).ifEmpty { listOf("img_activity_running") }
        val cleanBenefits = benefits.map { it.trim() }.filter { it.isNotBlank() }.take(12)
            .ifEmpty { listOf("Akses penuh ke aktivitas & grup peserta") }

        if (cleanTitle.length < 3) return Result.failure(IllegalArgumentException("Judul aktivitas minimal 3 karakter."))
        if (cleanDesc.length < 10) return Result.failure(IllegalArgumentException("Deskripsi aktivitas minimal 10 karakter."))
        if (cleanLoc.length < 2) return Result.failure(IllegalArgumentException("Lokasi aktivitas wajib diisi."))
        if (quota !in 1..10000) return Result.failure(IllegalArgumentException("Kuota peserta harus antara 1 sampai 10.000."))
        if (priceIdr !in 0..100_000_000) return Result.failure(IllegalArgumentException("Harga tidak valid."))
        if (publishNow && !organizerProfile.organizerVerified) {
            return Result.failure(IllegalStateException("Verifikasi dokumen KTP di Kelola Dokumen terlebih dahulu sebelum menerbitkan aktivitas."))
        }

        val targetStatus = if (publishNow) "PUBLISHED" else "DRAFT"
        val now = Timestamp.now()

        if (existingId.isNullOrBlank()) {
            val newId = "dev_act_${UUID.randomUUID().toString().take(8)}"
            val created = ActivityItem(
                id = newId,
                organizerId = uid,
                organizerName = organizerProfile.displayName.ifBlank { "Penyelenggara Diajak" }.take(80),
                organizerAvatarKey = organizerProfile.photoUrl.take(500),
                organizerVerified = organizerProfile.organizerVerified,
                title = cleanTitle,
                description = cleanDesc,
                category = niche.name,
                categoryEmoji = niche.emoji,
                city = cityOption.name,
                locationName = cleanLoc,
                latitude = cityOption.latitude,
                longitude = cityOption.longitude,
                eventDateMillis = eventDateMillis,
                timeLabel = timeLabel.trim().take(40).ifBlank { "08:00 WIB" },
                priceIdr = priceIdr,
                quota = quota,
                joinedCount = 0L,
                favoriteCount = 0L,
                viralScore = 50L,
                photoKeys = cleanPhotos,
                benefits = cleanBenefits,
                joinedUserNames = emptyList(),
                status = targetStatus,
                averageRating = 0.0,
                reviewCount = 0L,
                seededSample = false,
                createdAt = now,
                updatedAt = now
            )
            _activities.update { listOf(created) + it }
            return Result.success(newId)
        } else {
            _activities.update { list ->
                list.map { item ->
                    if (item.id != existingId) {
                        item
                    } else {
                        item.copy(
                            title = cleanTitle,
                            description = cleanDesc,
                            category = niche.name,
                            categoryEmoji = niche.emoji,
                            city = cityOption.name,
                            locationName = cleanLoc,
                            latitude = cityOption.latitude,
                            longitude = cityOption.longitude,
                            eventDateMillis = eventDateMillis,
                            timeLabel = timeLabel.trim().take(40).ifBlank { "08:00 WIB" },
                            priceIdr = priceIdr,
                            quota = quota,
                            photoKeys = cleanPhotos,
                            benefits = cleanBenefits,
                            status = targetStatus,
                            organizerVerified = organizerProfile.organizerVerified,
                            updatedAt = now
                        )
                    }
                }
            }
            return Result.success(existingId)
        }
    }

    override suspend fun updateActivityStatus(
        activityId: String,
        newStatus: String,
        organizerVerified: Boolean
    ): Result<Unit> {
        if (newStatus == "PUBLISHED" && !organizerVerified) {
            return Result.failure(IllegalStateException("Unggah & verifikasi KTP di Kelola Dokumen untuk menerbitkan aktivitas."))
        }
        _activities.update { list ->
            list.map { item ->
                if (item.id == activityId) {
                    item.copy(
                        status = newStatus,
                        organizerVerified = organizerVerified,
                        updatedAt = Timestamp.now()
                    )
                } else {
                    item
                }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun completeBooking(
        activity: ActivityItem,
        userProfile: UserProfile,
        ticketCount: Int,
        paymentMethodCode: String
    ): Result<BookingTicket> {
        val count = ticketCount.coerceIn(1, 10)
        val totalPrice = activity.priceIdr * count
        val method = if (totalPrice == 0L) "GRATIS" else paymentMethodCode
        val ticketCode = "DJK-${Random.nextInt(100000, 999999)}"
        val participantName = userProfile.displayName.ifBlank { "Developer Diajak" }.take(80)
        val bookingId = "dev_bkg_${UUID.randomUUID().toString().take(8)}"
        val now = Timestamp.now()

        val created = BookingTicket(
            id = bookingId,
            activityId = activity.id,
            activityTitle = activity.title.take(120),
            activityPhotoKey = (activity.photoKeys.firstOrNull() ?: "img_activity_running").take(500),
            activityLocation = activity.locationName.take(150),
            activityDateMillis = activity.eventDateMillis,
            activityTimeLabel = activity.timeLabel.take(40),
            userId = uid,
            userName = participantName,
            organizerId = activity.organizerId,
            organizerName = activity.organizerName.take(80),
            ticketCount = count.toLong(),
            totalPriceIdr = totalPrice,
            paymentMethod = method,
            ticketCode = ticketCode,
            status = "CONFIRMED",
            createdAt = now,
            updatedAt = now
        )
        _bookings.update { listOf(created) + it }

        val updatedNames = (listOf(participantName) + activity.joinedUserNames).distinct().take(20)
        _activities.update { list ->
            list.map { item ->
                if (item.id == activity.id) {
                    item.copy(
                        joinedCount = item.joinedCount + count,
                        joinedUserNames = updatedNames,
                        viralScore = item.viralScore + 15L,
                        updatedAt = now
                    )
                } else {
                    item
                }
            }
        }

        // Join or create local Activity Group Chat
        val convId = "group_${activity.id}".take(128)
        val organizerId = activity.organizerId.ifBlank { uid }
        val participants = listOf(uid, organizerId).distinct()
        val welcomeMsg = "$participantName bergabung ke aktivitas ${activity.title.take(60)}"
        _conversations.update { current ->
            val exists = current.any { it.id == convId }
            if (exists) {
                current.map { conv ->
                    if (conv.id == convId) {
                        conv.copy(
                            participantUids = (conv.participantUids + uid).distinct(),
                            lastMessage = welcomeMsg.take(500),
                            lastMessageSenderName = participantName,
                            updatedAt = now
                        )
                    } else {
                        conv
                    }
                }
            } else {
                listOf(
                    Conversation(
                        id = convId,
                        type = "GROUP",
                        activityId = activity.id,
                        activityTitle = activity.title.take(120),
                        title = "Grup: ${activity.title}".take(120),
                        organizerId = organizerId,
                        participantUids = participants,
                        lastMessage = welcomeMsg.take(500),
                        lastMessageSenderName = participantName,
                        createdAt = now,
                        updatedAt = now
                    )
                ) + current
            }
        }
        appendLocalMessage(
            conversationId = convId,
            senderName = participantName,
            text = "Halo semuanya! Saya baru saja memesan tiket untuk ${activity.title.take(80)}.",
            participantUids = participants
        )

        if (totalPrice > 0L && activity.organizerId == uid) {
            _walletTransactions.update { list ->
                listOf(
                    WalletTransaction(
                        id = "dev_tx_${UUID.randomUUID().toString().take(8)}",
                        userId = uid,
                        type = "INCOME",
                        amount = totalPrice,
                        title = "Tiket ${activity.title.take(60)} ($participantName)",
                        referenceId = bookingId,
                        status = "SUCCESS",
                        createdAt = now
                    )
                ) + list
            }
        }

        pushNotification(
            title = "E-Tiket Terbit: ${activity.title.take(50)}",
            body = "Booking $count tiket berhasil ($method). Kode tiket: $ticketCode. Kamu otomatis masuk ke Grup Chat aktivitas.",
            type = "BOOKING",
            relatedId = bookingId
        )

        return Result.success(created)
    }

    override suspend fun checkInBooking(bookingId: String): Result<Unit> {
        _bookings.update { list ->
            list.map { item ->
                if (item.id == bookingId) item.copy(status = "CHECKED_IN", updatedAt = Timestamp.now()) else item
            }
        }
        return Result.success(Unit)
    }

    override suspend fun cancelBooking(bookingId: String): Result<Unit> {
        _bookings.update { list ->
            list.map { item ->
                if (item.id == bookingId) item.copy(status = "CANCELLED", updatedAt = Timestamp.now()) else item
            }
        }
        return Result.success(Unit)
    }

    override suspend fun openOrCreateDirectChat(
        userName: String,
        activity: ActivityItem
    ): Result<String> {
        val sortedPair = listOf(uid, activity.organizerId).distinct().sorted()
        val rawId = "direct_${sortedPair.joinToString("_")}_${activity.id}"
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(120)
        val now = Timestamp.now()
        _conversations.update { current ->
            if (current.any { it.id == rawId }) {
                current
            } else {
                listOf(
                    Conversation(
                        id = rawId,
                        type = "DIRECT",
                        activityId = activity.id,
                        activityTitle = activity.title.take(120),
                        title = activity.organizerName.take(120),
                        organizerId = activity.organizerId,
                        participantUids = listOf(uid, activity.organizerId).distinct(),
                        lastMessage = "Memulai percakapan tentang ${activity.title.take(80)}",
                        lastMessageSenderName = userName.take(80),
                        createdAt = now,
                        updatedAt = now
                    )
                ) + current
            }
        }
        return Result.success(rawId)
    }

    override suspend fun sendMessage(
        conversation: Conversation,
        senderName: String,
        text: String
    ): Result<Unit> {
        val cleanText = text.trim().take(1000)
        if (cleanText.isEmpty()) {
            return Result.failure(IllegalArgumentException("Pesan tidak boleh kosong."))
        }
        val cleanSender = senderName.ifBlank { "Developer Diajak" }.take(80)
        val participants = (conversation.participantUids + uid).distinct().take(100)
        appendLocalMessage(
            conversationId = conversation.id,
            senderName = cleanSender,
            text = cleanText,
            participantUids = participants
        )
        val now = Timestamp.now()
        _conversations.update { current ->
            current.map { conv ->
                if (conv.id == conversation.id) {
                    conv.copy(
                        lastMessage = cleanText.take(500),
                        lastMessageSenderName = cleanSender,
                        updatedAt = now
                    )
                } else {
                    conv
                }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun addReview(
        activity: ActivityItem,
        authorName: String,
        rating: Int,
        comment: String
    ): Result<Unit> {
        val cleanComment = comment.trim().take(600)
        val safeRating = rating.coerceIn(1, 5)
        if (cleanComment.length < 2) {
            return Result.failure(IllegalArgumentException("Tulis ulasan minimal 2 karakter."))
        }
        val now = Timestamp.now()
        val review = ActivityReview(
            id = "dev_rev_${UUID.randomUUID().toString().take(8)}",
            activityId = activity.id,
            organizerId = activity.organizerId,
            authorId = uid,
            authorName = authorName.ifBlank { "Developer Diajak" }.take(80),
            rating = safeRating.toLong(),
            comment = cleanComment,
            organizerReply = "",
            createdAt = now,
            updatedAt = now
        )
        _reviews.update { map ->
            val updated = listOf(review) + map[activity.id].orEmpty()
            map + (activity.id to updated)
        }
        _activities.update { list ->
            list.map { item ->
                if (item.id == activity.id) {
                    val newCount = item.reviewCount + 1L
                    val newAvg = (((item.averageRating * item.reviewCount) + safeRating) / newCount.toDouble())
                        .coerceIn(0.0, 5.0)
                    item.copy(
                        averageRating = newAvg,
                        reviewCount = newCount,
                        updatedAt = now
                    )
                } else {
                    item
                }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun replyToReview(
        activityId: String,
        reviewId: String,
        replyText: String
    ): Result<Unit> {
        val cleanReply = replyText.trim().take(600)
        if (cleanReply.isEmpty()) {
            return Result.failure(IllegalArgumentException("Balasan ulasan tidak boleh kosong."))
        }
        _reviews.update { map ->
            val updated = map[activityId].orEmpty().map { rev ->
                if (rev.id == reviewId) {
                    rev.copy(organizerReply = cleanReply, updatedAt = Timestamp.now())
                } else {
                    rev
                }
            }
            map + (activityId to updated)
        }
        return Result.success(Unit)
    }

    override suspend fun requestWithdrawal(
        amount: Long,
        destinationLabel: String,
        availableBalance: Long
    ): Result<Unit> {
        if (amount < 10_000L) return Result.failure(IllegalArgumentException("Minimal penarikan saldo adalah Rp 10.000."))
        if (amount > availableBalance) return Result.failure(IllegalArgumentException("Saldo tersedia tidak mencukupi."))
        val cleanDest = destinationLabel.trim().take(100)
        if (cleanDest.length < 3) return Result.failure(IllegalArgumentException("Masukkan tujuan rekening / e-wallet yang valid."))

        val txId = "dev_wd_${UUID.randomUUID().toString().take(8)}"
        _walletTransactions.update { list ->
            listOf(
                WalletTransaction(
                    id = txId,
                    userId = uid,
                    type = "WITHDRAWAL",
                    amount = amount,
                    title = "Penarikan ke $cleanDest".take(150),
                    referenceId = "WD-${Random.nextInt(10000, 99999)}",
                    status = "SUCCESS",
                    createdAt = Timestamp.now()
                )
            ) + list
        }
        pushNotification(
            title = "Penarikan Saldo Berhasil",
            body = "Dana sebesar ${DiajakCatalog.formatRupiah(amount)} telah diproses ke $cleanDest.",
            type = "WALLET",
            relatedId = txId
        )
        return Result.success(Unit)
    }

    override suspend fun markNotificationRead(notifId: String): Result<Unit> {
        _notifications.update { list ->
            list.map { if (it.id == notifId) it.copy(readState = true) else it }
        }
        return Result.success(Unit)
    }

    private fun appendLocalMessage(
        conversationId: String,
        senderName: String,
        text: String,
        participantUids: List<String>
    ) {
        val msg = ChatMessage(
            id = "dev_msg_${UUID.randomUUID().toString().take(8)}",
            conversationId = conversationId,
            senderId = uid,
            senderName = senderName,
            text = text,
            participantUids = participantUids,
            createdAt = Timestamp.now()
        )
        _messages.update { map ->
            map + (conversationId to (map[conversationId].orEmpty() + msg))
        }
    }

    private fun pushNotification(
        title: String,
        body: String,
        type: String,
        relatedId: String
    ) {
        _notifications.update { list ->
            listOf(
                AppNotification(
                    id = "dev_notif_${UUID.randomUUID().toString().take(8)}",
                    title = title.take(120),
                    body = body.take(400),
                    type = type,
                    relatedId = relatedId.take(128),
                    readState = false,
                    createdAt = Timestamp.now()
                )
            ) + list
        }
    }
}
