package com.example.data

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface DiajakDataSource {
    fun observeUserProfile(uid: String): Flow<UserProfile?>
    fun observeKycDocument(uid: String): Flow<KycDocument?>
    fun observePublishedActivities(uid: String?): Flow<List<ActivityItem>>
    fun observeOrganizerActivities(uid: String): Flow<List<ActivityItem>>
    fun observeFavorites(uid: String): Flow<Set<String>>
    fun observeNotifications(uid: String): Flow<List<AppNotification>>
    fun observeWalletTransactions(uid: String): Flow<List<WalletTransaction>>
    fun observeParticipantBookings(uid: String): Flow<List<BookingTicket>>
    fun observeOrganizerBookings(uid: String): Flow<List<BookingTicket>>
    fun observeConversations(uid: String): Flow<List<Conversation>>
    fun observeMessages(conversationId: String, uid: String): Flow<List<ChatMessage>>
    fun observeActivityReviews(activityId: String, uid: String?): Flow<List<ActivityReview>>

    suspend fun ensureUserProfileAndSeedCatalog(user: FirebaseUser): Result<Unit>

    suspend fun updateUserProfile(
        displayName: String,
        bio: String,
        city: String,
        photoUrl: String,
        interests: List<String>
    ): Result<Unit>

    suspend fun updatePreferredCity(city: String): Result<Unit>

    suspend fun submitKycDocument(
        fullName: String,
        nikNumber: String,
        documentPhotoUri: String
    ): Result<Unit>

    suspend fun toggleFavorite(activity: ActivityItem, currentlyFavorited: Boolean): Result<Unit>

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
    ): Result<String>

    suspend fun updateActivityStatus(
        activityId: String,
        newStatus: String,
        organizerVerified: Boolean
    ): Result<Unit>

    suspend fun completeBooking(
        activity: ActivityItem,
        userProfile: UserProfile,
        ticketCount: Int,
        paymentMethodCode: String
    ): Result<BookingTicket>

    suspend fun checkInBooking(bookingId: String): Result<Unit>
    suspend fun cancelBooking(bookingId: String): Result<Unit>

    suspend fun openOrCreateDirectChat(
        userName: String,
        activity: ActivityItem
    ): Result<String>

    suspend fun sendMessage(
        conversation: Conversation,
        senderName: String,
        text: String
    ): Result<Unit>

    suspend fun addReview(
        activity: ActivityItem,
        authorName: String,
        rating: Int,
        comment: String
    ): Result<Unit>

    suspend fun replyToReview(
        activityId: String,
        reviewId: String,
        replyText: String
    ): Result<Unit>

    suspend fun requestWithdrawal(
        amount: Long,
        destinationLabel: String,
        availableBalance: Long
    ): Result<Unit>

    suspend fun markNotificationRead(notifId: String): Result<Unit>
}
