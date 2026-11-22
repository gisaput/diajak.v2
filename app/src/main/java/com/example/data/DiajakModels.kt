package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class PaymentMethodOption(val code: String, val label: String, val subtitle: String) {
    QRIS("QRIS", "QRIS", "Scan QR semua bank & e-wallet"),
    GOPAY("GOPAY", "GoPay", "Bayar instan via GoPay"),
    OVO("OVO", "OVO", "Bayar instan via OVO"),
    DANA("DANA", "DANA", "Bayar instan via DANA"),
    SHOPEEPAY("SHOPEEPAY", "ShopeePay", "Bayar instan via ShopeePay")
}

data class NicheCategory(
    val name: String,
    val emoji: String
)

data class CityOption(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val bio: String = "",
    val city: String = "Jakarta Selatan",
    val photoUrl: String = "",
    val interests: List<String> = emptyList(),
    val organizerVerified: Boolean = false,
    val kycStatus: String = "UNVERIFIED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class KycDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val nikNumber: String = "",
    val documentPhotoUri: String = "",
    val status: String = "VERIFIED",
    @ServerTimestamp val submittedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FavoriteItem(
    @DocumentId val id: String = "",
    val activityId: String = "",
    @ServerTimestamp val savedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "SYSTEM",
    val relatedId: String = "",
    val readState: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = "INCOME",
    val amount: Long = 0L,
    val title: String = "",
    val referenceId: String = "",
    val status: String = "SUCCESS",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ActivityItem(
    @DocumentId val id: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val organizerAvatarKey: String = "",
    val organizerVerified: Boolean = false,
    val title: String = "",
    val description: String = "",
    val category: String = "Olahraga",
    val categoryEmoji: String = "⚽",
    val city: String = "Jakarta Selatan",
    val locationName: String = "",
    val latitude: Double = -6.2297,
    val longitude: Double = 106.8095,
    val eventDateMillis: Long = 0L,
    val timeLabel: String = "07:00 WIB",
    val priceIdr: Long = 0L,
    val quota: Long = 30L,
    val joinedCount: Long = 0L,
    val favoriteCount: Long = 0L,
    val viralScore: Long = 0L,
    val photoKeys: List<String> = listOf("img_activity_running"),
    val benefits: List<String> = emptyList(),
    val joinedUserNames: List<String> = emptyList(),
    val status: String = "PUBLISHED",
    val averageRating: Double = 0.0,
    val reviewCount: Long = 0L,
    val seededSample: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ActivityReview(
    @DocumentId val id: String = "",
    val activityId: String = "",
    val organizerId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    val organizerReply: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BookingTicket(
    @DocumentId val id: String = "",
    val activityId: String = "",
    val activityTitle: String = "",
    val activityPhotoKey: String = "",
    val activityLocation: String = "",
    val activityDateMillis: Long = 0L,
    val activityTimeLabel: String = "",
    val userId: String = "",
    val userName: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val ticketCount: Long = 1L,
    val totalPriceIdr: Long = 0L,
    val paymentMethod: String = "GRATIS",
    val ticketCode: String = "",
    val status: String = "CONFIRMED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val type: String = "DIRECT",
    val activityId: String = "",
    val activityTitle: String = "",
    val title: String = "",
    val organizerId: String = "",
    val participantUids: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageSenderName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val participantUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)
