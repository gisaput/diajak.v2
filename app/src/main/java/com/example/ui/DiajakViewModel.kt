package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityItem
import com.example.data.ActivityReview
import com.example.data.AppNotification
import com.example.data.BookingTicket
import com.example.data.ChatMessage
import com.example.data.CityOption
import com.example.data.Conversation
import com.example.data.DiajakCatalog
import com.example.data.DiajakDataSource
import com.example.data.FeedDynamicSignals
import com.example.data.FeedRankingEngine
import com.example.data.KycDocument
import com.example.data.LocationService
import com.example.data.NicheCategory
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.data.WalletTransaction
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedFilters(
    val searchQuery: String = "",
    val selectedNiche: String? = null
)

data class ActiveLocationState(
    val cityName: String = "Jakarta Selatan",
    val latitude: Double = -6.2615,
    val longitude: Double = 106.8106,
    val isGpsActive: Boolean = false
)

data class PagedFeedResult(
    val items: List<ActivityItem>,
    val totalCount: Int,
    val hasMore: Boolean,
    val isLoadingMore: Boolean
)

class DiajakViewModel(
    private val repository: DiajakDataSource,
    private val locationService: LocationService,
    val currentUserId: String?
) : ViewModel() {

    val isSignedIn: Boolean get() = !currentUserId.isNullOrBlank()

    private val fallbackShowcaseItems: List<ActivityItem> by lazy {
        DiajakCatalog.buildShowcaseActivityItems()
    }

    private var bootstrapJob: Job? = null

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    private val _filters = MutableStateFlow(FeedFilters())
    val filters: StateFlow<FeedFilters> = _filters.asStateFlow()

    private val _locationState = MutableStateFlow(ActiveLocationState())
    val locationState: StateFlow<ActiveLocationState> = _locationState.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _isRefreshingFeed = MutableStateFlow(false)
    val isRefreshingFeed: StateFlow<Boolean> = _isRefreshingFeed.asStateFlow()

    private val _visibleFeedCount = MutableStateFlow(INITIAL_FEED_BATCH_SIZE)
    private val _isLoadingMoreFeed = MutableStateFlow(false)
    private var loadMoreFeedJob: Job? = null

    private val _dynamicSignals = MutableStateFlow(FeedDynamicSignals())

    private var autoLocationPermissionRequested = false

    private fun <T> Flow<T>.toSafeUiState(tag: String, fallbackMsg: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "$tag error", rootError)
                emit(UiState.Error(rootError.message ?: fallbackMsg))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        if (currentUserId != null) {
            repository.observeUserProfile(currentUserId)
                .toSafeUiState("profile", "Gagal memuat profil.")
        } else {
            MutableStateFlow(UiState.Success(null))
        }

    val kycDocumentState: StateFlow<UiState<KycDocument?>> =
        if (currentUserId != null) {
            repository.observeKycDocument(currentUserId)
                .toSafeUiState("kyc", "Gagal memuat dokumen KTP.")
        } else {
            MutableStateFlow(UiState.Success(null))
        }

    val publishedActivitiesState: StateFlow<UiState<List<ActivityItem>>> =
        repository.observePublishedActivities(currentUserId)
            .map<List<ActivityItem>, UiState<List<ActivityItem>>> { items ->
                // If marketplace has not been seeded yet by the first signed-in user,
                // show the curated upcoming showcase catalog so guests can explore immediately.
                UiState.Success(items.ifEmpty { fallbackShowcaseItems })
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "activities error, using showcase fallback", firestoreError ?: error)
                emit(UiState.Success(fallbackShowcaseItems))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val organizerActivitiesState: StateFlow<UiState<List<ActivityItem>>> =
        if (currentUserId != null) {
            repository.observeOrganizerActivities(currentUserId)
                .toSafeUiState("organizer_activities", "Gagal memuat aktivitas penyelenggara.")
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    val favoriteIdsState: StateFlow<Set<String>> =
        if (currentUserId != null) {
            repository.observeFavorites(currentUserId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    emit(emptySet())
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptySet())
        } else {
            MutableStateFlow(emptySet())
        }

    val notificationsState: StateFlow<List<AppNotification>> =
        if (currentUserId != null) {
            repository.observeNotifications(currentUserId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    emit(emptyList())
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())
        } else {
            MutableStateFlow(emptyList())
        }

    val walletTransactionsState: StateFlow<List<WalletTransaction>> =
        if (currentUserId != null) {
            repository.observeWalletTransactions(currentUserId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    emit(emptyList())
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())
        } else {
            MutableStateFlow(emptyList())
        }

    val participantBookingsState: StateFlow<UiState<List<BookingTicket>>> =
        if (currentUserId != null) {
            repository.observeParticipantBookings(currentUserId)
                .toSafeUiState("participant_bookings", "Gagal memuat daftar tiket.")
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    val organizerBookingsState: StateFlow<UiState<List<BookingTicket>>> =
        if (currentUserId != null) {
            repository.observeOrganizerBookings(currentUserId)
                .toSafeUiState("organizer_bookings", "Gagal memuat data pendaftaran.")
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    val conversationsState: StateFlow<UiState<List<Conversation>>> =
        if (currentUserId != null) {
            repository.observeConversations(currentUserId)
                .toSafeUiState("conversations", "Gagal memuat pesan.")
        } else {
            MutableStateFlow(UiState.Success(emptyList()))
        }

    // Dynamic TikTok/Instagram-style Beranda feed:
    // Priority 1: Popularity & crowd momentum | Priority 2: Non-expired & upcoming urgency |
    // Priority 3: Nearest to user location + personal affinity, quality, diversity & pull-to-refresh freshness
    val feedActivitiesState: StateFlow<UiState<List<ActivityItem>>> = combine(
        publishedActivitiesState,
        filters,
        locationState,
        combine(userProfileState, favoriteIdsState, _dynamicSignals) { profileState, favIds, signals ->
            Triple(profileState, favIds, signals)
        }
    ) { rawState, activeFilters, loc, personalContext ->
        val (profileState, favIds, signals) = personalContext
        when (rawState) {
            is UiState.Loading -> UiState.Loading
            is UiState.Error -> rawState
            is UiState.Success -> {
                val interests = (profileState as? UiState.Success)?.data?.interests.orEmpty()
                val ranked = FeedRankingEngine.rankFeed(
                    activities = rawState.data,
                    nowMillis = System.currentTimeMillis(),
                    userLat = loc.latitude,
                    userLng = loc.longitude,
                    userCity = loc.cityName,
                    searchQuery = activeFilters.searchQuery,
                    selectedNiche = activeFilters.selectedNiche,
                    userInterests = interests,
                    favoriteActivityIds = favIds,
                    signals = signals
                )
                UiState.Success(ranked)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val pagedFeedState: StateFlow<UiState<PagedFeedResult>> = combine(
        feedActivitiesState,
        _visibleFeedCount,
        _isLoadingMoreFeed
    ) { fullState, visibleCount, isLoadingMore ->
        when (fullState) {
            is UiState.Loading -> UiState.Loading
            is UiState.Error -> fullState
            is UiState.Success -> {
                val allItems = fullState.data
                val clampedCount = visibleCount.coerceAtMost(allItems.size)
                val hasMore = clampedCount < allItems.size
                UiState.Success(
                    PagedFeedResult(
                        items = allItems.take(clampedCount),
                        totalCount = allItems.size,
                        hasMore = hasMore,
                        isLoadingMore = isLoadingMore && hasMore
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val availableWalletBalance: StateFlow<Long> = walletTransactionsState
        .map { txs ->
            val income = txs.filter { it.type == "INCOME" }.sumOf { it.amount }
            val withdrawals = txs.filter { it.type == "WITHDRAWAL" }.sumOf { it.amount }
            (income - withdrawals).coerceAtLeast(0L)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0L)

    val unreadNotificationCount: StateFlow<Int> = notificationsState
        .map { list -> list.count { !it.readState } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    init {
        if (locationService.hasLocationPermission()) {
            detectGpsLocation(silent = true)
        }
        if (currentUserId != null) {
            bootstrapJob = viewModelScope.launch {
                val user = Firebase.auth.currentUser
                if (user != null) {
                    repository.ensureUserProfileAndSeedCatalog(user)
                }
            }
            viewModelScope.launch {
                userProfileState.collect { state ->
                    if (state is UiState.Success && state.data != null && !_locationState.value.isGpsActive) {
                        val city = DiajakCatalog.findCityByName(state.data.city)
                        _locationState.value = ActiveLocationState(
                            cityName = city.name,
                            latitude = city.latitude,
                            longitude = city.longitude,
                            isGpsActive = false
                        )
                    }
                }
            }
        }
    }

    fun observeReviews(activityId: String): Flow<List<ActivityReview>> =
        repository.observeActivityReviews(activityId, currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                emit(emptyList())
            }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> {
        val uid = currentUserId ?: return flowOf(emptyList())
        return repository.observeMessages(conversationId, uid)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                emit(emptyList())
            }
    }

    fun distanceToActivityKm(activity: ActivityItem): Double {
        val loc = _locationState.value
        return DiajakCatalog.distanceKm(loc.latitude, loc.longitude, activity.latitude, activity.longitude)
    }

    // --- FILTER & LOCATION ACTIONS ---

    fun updateSearchQuery(query: String) {
        _filters.value = _filters.value.copy(searchQuery = query)
        resetFeedPagination()
    }

    fun selectNiche(nicheName: String?) {
        _filters.value = _filters.value.copy(selectedNiche = nicheName)
        resetFeedPagination()
        if (!nicheName.isNullOrBlank()) {
            recordCategoryAffinity(nicheName, weight = 1)
        }
    }

    private fun resetFeedPagination() {
        loadMoreFeedJob?.cancel()
        _isLoadingMoreFeed.value = false
        _visibleFeedCount.value = INITIAL_FEED_BATCH_SIZE
    }

    fun loadMoreFeedActivities() {
        val currentState = feedActivitiesState.value as? UiState.Success ?: return
        if (_isLoadingMoreFeed.value || _isRefreshingFeed.value) return
        if (_visibleFeedCount.value >= currentState.data.size) return

        loadMoreFeedJob?.cancel()
        loadMoreFeedJob = viewModelScope.launch {
            _isLoadingMoreFeed.value = true
            delay(FEED_LOAD_MORE_DELAY_MS)
            val latestTotal = (feedActivitiesState.value as? UiState.Success)?.data?.size
                ?: currentState.data.size
            _visibleFeedCount.update { current ->
                (current + FEED_BATCH_INCREMENT).coerceAtMost(latestTotal)
            }
            _isLoadingMoreFeed.value = false
        }
    }

    fun recordActivityInteraction(activity: ActivityItem, weight: Int = 1) {
        val catKey = activity.category.trim().lowercase()
        _dynamicSignals.update { current ->
            val updatedClicks = current.categoryClickCounts.toMutableMap()
            if (catKey.isNotEmpty()) {
                updatedClicks[catKey] = (updatedClicks[catKey] ?: 0) + weight
            }
            current.copy(
                categoryClickCounts = updatedClicks,
                viewedActivityIds = current.viewedActivityIds + activity.id
            )
        }
    }

    private fun recordCategoryAffinity(categoryName: String, weight: Int = 1) {
        val catKey = categoryName.trim().lowercase()
        if (catKey.isEmpty()) return
        _dynamicSignals.update { current ->
            val updatedClicks = current.categoryClickCounts.toMutableMap()
            updatedClicks[catKey] = (updatedClicks[catKey] ?: 0) + weight
            current.copy(categoryClickCounts = updatedClicks)
        }
    }

    fun refreshDynamicFeed() {
        if (_isRefreshingFeed.value) return
        loadMoreFeedJob?.cancel()
        _isLoadingMoreFeed.value = false
        viewModelScope.launch {
            _isRefreshingFeed.value = true
            if (locationService.hasLocationPermission()) {
                detectGpsLocation(silent = true)
            }
            delay(FEED_REFRESH_DELAY_MS)
            _visibleFeedCount.value = INITIAL_FEED_BATCH_SIZE
            _dynamicSignals.update { current ->
                current.copy(
                    refreshSeed = System.nanoTime(),
                    refreshCount = current.refreshCount + 1
                )
            }
            _isRefreshingFeed.value = false
        }
    }

    fun hasLocationPermission(): Boolean = locationService.hasLocationPermission()

    fun shouldRequestAutoLocationPermission(): Boolean =
        !autoLocationPermissionRequested && !locationService.hasLocationPermission()

    fun markAutoLocationPermissionRequested() {
        autoLocationPermissionRequested = true
    }

    fun selectManualCity(city: CityOption) {
        _locationState.value = ActiveLocationState(
            cityName = city.name,
            latitude = city.latitude,
            longitude = city.longitude,
            isGpsActive = false
        )
        viewModelScope.launch {
            if (isSignedIn) {
                repository.updatePreferredCity(city.name)
            }
            _toastEvents.tryEmit("Lokasi diubah ke ${city.name}")
        }
    }

    fun detectGpsLocation(silent: Boolean = false) {
        viewModelScope.launch {
            val detected = locationService.detectCurrentLocation()
            if (detected != null) {
                _locationState.value = ActiveLocationState(
                    cityName = detected.nearestCityName,
                    latitude = detected.latitude,
                    longitude = detected.longitude,
                    isGpsActive = true
                )
                if (isSignedIn) {
                    repository.updatePreferredCity(detected.nearestCityName)
                }
                if (!silent) {
                    _toastEvents.tryEmit("Lokasi terdeteksi: ${detected.nearestCityName}")
                }
            } else if (!silent) {
                _toastEvents.tryEmit("Lokasi belum tersedia saat ini.")
            }
        }
    }

    // --- USER ACTIONS ---

    fun toggleFavorite(activity: ActivityItem) {
        val currentlyFav = favoriteIdsState.value.contains(activity.id)
        if (!currentlyFav) {
            recordActivityInteraction(activity, weight = 2)
        }
        viewModelScope.launch {
            bootstrapJob?.join()
            val res = repository.toggleFavorite(activity, currentlyFav)
            res.onFailure { _toastEvents.tryEmit(it.message ?: "Gagal memperbarui favorit.") }
        }
    }

    fun updateProfile(
        displayName: String,
        bio: String,
        city: String,
        photoUrl: String,
        interests: List<String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.updateUserProfile(displayName, bio, city, photoUrl, interests)
            _isBusy.value = false
            res.onSuccess {
                _toastEvents.tryEmit("Profil berhasil diperbarui.")
                onSuccess()
            }.onFailure {
                _toastEvents.tryEmit(it.message ?: "Gagal memperbarui profil.")
            }
        }
    }

    fun submitKyc(
        fullName: String,
        nikNumber: String,
        documentPhotoUri: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.submitKycDocument(fullName, nikNumber, documentPhotoUri)
            _isBusy.value = false
            res.onSuccess {
                _toastEvents.tryEmit("Dokumen KTP terverifikasi! Sekarang kamu bisa menerbitkan aktivitas.")
                onSuccess()
            }.onFailure {
                _toastEvents.tryEmit(it.message ?: "Gagal memverifikasi dokumen KTP.")
            }
        }
    }

    fun saveOrganizerActivity(
        existingId: String?,
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
        publishNow: Boolean,
        onSuccess: () -> Unit
    ) {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(
            userId = currentUserId.orEmpty(),
            displayName = Firebase.auth.currentUser?.displayName ?: "Penyelenggara"
        )
        viewModelScope.launch {
            bootstrapJob?.join()
            _isBusy.value = true
            val res = repository.saveActivity(
                existingId = existingId,
                organizerProfile = profile,
                title = title,
                description = description,
                niche = niche,
                cityOption = cityOption,
                locationName = locationName,
                eventDateMillis = eventDateMillis,
                timeLabel = timeLabel,
                priceIdr = priceIdr,
                quota = quota,
                photoKeys = photoKeys,
                benefits = benefits,
                publishNow = publishNow
            )
            _isBusy.value = false
            res.onSuccess {
                val msg = if (publishNow) "Aktivitas berhasil diterbitkan ke Beranda!" else "Aktivitas disimpan sebagai Draft."
                _toastEvents.tryEmit(msg)
                onSuccess()
            }.onFailure {
                _toastEvents.tryEmit(it.message ?: "Gagal menyimpan aktivitas.")
            }
        }
    }

    fun changeActivityStatus(activityId: String, newStatus: String) {
        val verified = (userProfileState.value as? UiState.Success)?.data?.organizerVerified == true
        viewModelScope.launch {
            val res = repository.updateActivityStatus(activityId, newStatus, verified)
            res.onSuccess {
                val label = when (newStatus) {
                    "PUBLISHED" -> "Aktivitas diterbitkan!"
                    "CLOSED" -> "Aktivitas ditutup."
                    else -> "Status aktivitas diperbarui."
                }
                _toastEvents.tryEmit(label)
            }.onFailure {
                _toastEvents.tryEmit(it.message ?: "Gagal mengubah status aktivitas.")
            }
        }
    }

    fun bookActivity(
        activity: ActivityItem,
        ticketCount: Int,
        paymentMethodCode: String,
        onBooked: (BookingTicket) -> Unit
    ) {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(
            userId = currentUserId.orEmpty(),
            displayName = Firebase.auth.currentUser?.displayName ?: "Peserta"
        )
        viewModelScope.launch {
            bootstrapJob?.join()
            _isBusy.value = true
            val res = repository.completeBooking(activity, profile, ticketCount, paymentMethodCode)
            _isBusy.value = false
            res.onSuccess { ticket ->
                _toastEvents.tryEmit("E-Tiket berhasil terbit! Kode: ${ticket.ticketCode}")
                onBooked(ticket)
            }.onFailure {
                _toastEvents.tryEmit(it.message ?: "Booking gagal, silakan coba lagi.")
            }
        }
    }

    fun checkInParticipantTicket(bookingId: String) {
        viewModelScope.launch {
            val res = repository.checkInBooking(bookingId)
            res.onSuccess { _toastEvents.tryEmit("Tiket peserta berhasil di-check-in!") }
                .onFailure { _toastEvents.tryEmit(it.message ?: "Gagal check-in tiket.") }
        }
    }

    fun cancelParticipantTicket(bookingId: String) {
        viewModelScope.launch {
            val res = repository.cancelBooking(bookingId)
            res.onSuccess { _toastEvents.tryEmit("Tiket berhasil dibatalkan.") }
                .onFailure { _toastEvents.tryEmit(it.message ?: "Gagal membatalkan tiket.") }
        }
    }

    fun openDirectChatWithOrganizer(activity: ActivityItem, onReady: (String) -> Unit) {
        val name = (userProfileState.value as? UiState.Success)?.data?.displayName
            ?: Firebase.auth.currentUser?.displayName ?: "Peserta"
        viewModelScope.launch {
            bootstrapJob?.join()
            val res = repository.openOrCreateDirectChat(name, activity)
            res.onSuccess { convId -> onReady(convId) }
                .onFailure { _toastEvents.tryEmit(it.message ?: "Gagal membuka chat penyelenggara.") }
        }
    }

    fun sendMessage(conversation: Conversation, text: String, onSent: () -> Unit = {}) {
        val name = (userProfileState.value as? UiState.Success)?.data?.displayName
            ?: Firebase.auth.currentUser?.displayName ?: "Pengguna"
        viewModelScope.launch {
            val res = repository.sendMessage(conversation, name, text)
            res.onSuccess { onSent() }
                .onFailure { _toastEvents.tryEmit(it.message ?: "Gagal mengirim pesan.") }
        }
    }

    fun submitReview(activity: ActivityItem, rating: Int, comment: String, onSuccess: () -> Unit) {
        val name = (userProfileState.value as? UiState.Success)?.data?.displayName
            ?: Firebase.auth.currentUser?.displayName ?: "Peserta"
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.addReview(activity, name, rating, comment)
            _isBusy.value = false
            res.onSuccess {
                _toastEvents.tryEmit("Ulasan berhasil dikirim!")
                onSuccess()
            }.onFailure {
                _toastEvents.tryEmit(it.message ?: "Gagal mengirim ulasan.")
            }
        }
    }

    fun submitOrganizerReply(activityId: String, reviewId: String, reply: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.replyToReview(activityId, reviewId, reply)
            res.onSuccess {
                _toastEvents.tryEmit("Balasan ulasan tersimpan.")
                onSuccess()
            }.onFailure {
                _toastEvents.tryEmit(it.message ?: "Gagal membalas ulasan.")
            }
        }
    }

    fun withdrawBalance(amount: Long, destination: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.requestWithdrawal(amount, destination, availableWalletBalance.value)
            _isBusy.value = false
            res.onSuccess {
                _toastEvents.tryEmit("Penarikan saldo berhasil diproses!")
                onSuccess()
            }.onFailure {
                _toastEvents.tryEmit(it.message ?: "Penarikan saldo gagal.")
            }
        }
    }

    fun markNotificationRead(notifId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notifId)
        }
    }

    companion object {
        private const val TAG = "DiajakVM"
        private const val STOP_TIMEOUT_MS = 5000L
        private const val INITIAL_FEED_BATCH_SIZE = 3
        private const val FEED_BATCH_INCREMENT = 2
        private const val FEED_REFRESH_DELAY_MS = 650L
        private const val FEED_LOAD_MORE_DELAY_MS = 680L
    }
}
