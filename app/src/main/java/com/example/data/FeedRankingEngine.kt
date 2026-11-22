package com.example.data

import kotlin.math.ln
import kotlin.math.min

/**
 * Dynamic social-feed ranking engine (TikTok / Instagram-style) for Beranda.
 *
 * Strict priority order:
 * 1. Popularity & crowd momentum (joinedCount, fill-rate, viralScore, favorites)
 * 2. Active non-expired schedule (hard gate: eventDateMillis > now) + upcoming urgency curve
 * 3. Proximity to user's GPS / selected city location
 *
 * Plus dynamic personalization & freshness signals:
 * - User interest & in-session interaction affinity (profile interests, favorites, tapped categories)
 * - Organizer trust & community rating quality
 * - Seat availability (deprioritize already sold-out events)
 * - Pull-to-refresh session exploration & anti-fatigue for already-viewed items
 * - Category diversity interleaving so the main feed never feels repetitive
 */
data class FeedDynamicSignals(
    val refreshSeed: Long = System.currentTimeMillis() / 60_000L,
    val refreshCount: Int = 0,
    val categoryClickCounts: Map<String, Int> = emptyMap(),
    val viewedActivityIds: Set<String> = emptySet()
)

object FeedRankingEngine {

    private const val HOUR_MS = 3_600_000.0
    private const val DAY_MS = 24.0 * HOUR_MS

    fun rankFeed(
        activities: List<ActivityItem>,
        nowMillis: Long,
        userLat: Double,
        userLng: Double,
        userCity: String,
        searchQuery: String,
        selectedNiche: String?,
        userInterests: List<String>,
        favoriteActivityIds: Set<String>,
        signals: FeedDynamicSignals
    ): List<ActivityItem> {
        val trimmedQuery = searchQuery.trim()

        // Priority 2 (Hard Gate): strictly active & non-expired schedules only
        val eligible = activities.filter { item ->
            val isPublished = item.status.equals("PUBLISHED", ignoreCase = true)
            val notExpired = item.eventDateMillis > nowMillis
            val matchesNiche = selectedNiche == null ||
                item.category.equals(selectedNiche, ignoreCase = true)
            val matchesQuery = trimmedQuery.isEmpty() ||
                item.title.contains(trimmedQuery, ignoreCase = true) ||
                item.locationName.contains(trimmedQuery, ignoreCase = true) ||
                item.city.contains(trimmedQuery, ignoreCase = true) ||
                item.category.contains(trimmedQuery, ignoreCase = true) ||
                item.organizerName.contains(trimmedQuery, ignoreCase = true)
            isPublished && notExpired && matchesNiche && matchesQuery
        }

        if (eligible.isEmpty()) return emptyList()

        // Build affinity category sets from user profile interests & favorited items
        val normalizedInterests = userInterests.map { it.trim().lowercase() }.toSet()
        val favoritedCategories = activities
            .asSequence()
            .filter { favoriteActivityIds.contains(it.id) }
            .map { it.category.trim().lowercase() }
            .toSet()

        // Normalization baselines across currently eligible catalog
        val maxJoinedLog = eligible.maxOf { ln(1.0 + it.joinedCount.coerceAtLeast(0L).toDouble()) }
            .coerceAtLeast(1.0)
        val maxViral = eligible.maxOf { (it.viralScore + it.favoriteCount * 3L).coerceAtLeast(0L) }
            .coerceAtLeast(1L)
            .toDouble()

        val scored = eligible.map { item ->
            val score = computeItemScore(
                item = item,
                nowMillis = nowMillis,
                userLat = userLat,
                userLng = userLng,
                userCity = userCity,
                maxJoinedLog = maxJoinedLog,
                maxViral = maxViral,
                normalizedInterests = normalizedInterests,
                favoritedCategories = favoritedCategories,
                signals = signals
            )
            item to score
        }.sortedByDescending { it.second }

        val rankedItems = scored.map { it.first }

        // Apply subtle category-diversity interleaving only on the unfiltered discovery feed
        return if (selectedNiche == null && trimmedQuery.isEmpty() && rankedItems.size > 3) {
            applyCategoryDiversity(rankedItems)
        } else {
            rankedItems
        }
    }

    private fun computeItemScore(
        item: ActivityItem,
        nowMillis: Long,
        userLat: Double,
        userLng: Double,
        userCity: String,
        maxJoinedLog: Double,
        maxViral: Double,
        normalizedInterests: Set<String>,
        favoritedCategories: Set<String>,
        signals: FeedDynamicSignals
    ): Double {
        // --- PRIORITY 1 (Dominant Weight ~50 pts): Crowd & Momentum ---
        val joinedLogNorm = ln(1.0 + item.joinedCount.coerceAtLeast(0L).toDouble()) / maxJoinedLog
        val joinedVolumeScore = joinedLogNorm * 26.0

        val safeQuota = item.quota.coerceAtLeast(1L).toDouble()
        val fillRatio = (item.joinedCount.toDouble() / safeQuota).coerceIn(0.0, 1.0)
        val isSoldOut = item.joinedCount >= item.quota && item.quota > 0L
        val crowdMomentumScore = if (isSoldOut) {
            -6.0 // Deprioritize sold-out events slightly so joinable activities lead
        } else {
            // Peak excitement when filling up steadily (e.g. 50%-95% full)
            fillRatio * 12.0
        }

        val rawViral = (item.viralScore + item.favoriteCount * 3L).coerceAtLeast(0L).toDouble()
        val viralEngagementScore = (rawViral / maxViral) * 12.0

        val priority1Score = joinedVolumeScore + crowdMomentumScore + viralEngagementScore

        // --- PRIORITY 2 (~22 pts on top of hard non-expired gate): Upcoming Schedule Urgency ---
        val millisUntilEvent = (item.eventDateMillis - nowMillis).coerceAtLeast(0L).toDouble()
        val daysUntilEvent = millisUntilEvent / DAY_MS
        val priority2Score = when {
            daysUntilEvent <= 2.0 -> 22.0
            daysUntilEvent <= 7.0 -> 22.0 - ((daysUntilEvent - 2.0) * 1.4) // 22 -> 15
            daysUntilEvent <= 21.0 -> 15.0 - ((daysUntilEvent - 7.0) * 0.5) // 15 -> 8
            else -> (8.0 / (1.0 + (daysUntilEvent - 21.0) / 30.0))
        }

        // --- PRIORITY 3 (~20 pts): Proximity to User Location ---
        val distKm = DiajakCatalog.distanceKm(userLat, userLng, item.latitude, item.longitude)
        val distanceDecayScore = 17.0 / (1.0 + distKm / 12.0)
        val sameCityBonus = if (item.city.equals(userCity, ignoreCase = true)) 3.0 else 0.0
        val priority3Score = distanceDecayScore + sameCityBonus

        // --- DYNAMIC SOCIAL SIGNALS (TikTok / Instagram-style) ---
        val catKey = item.category.trim().lowercase()
        var affinityScore = 0.0
        if (normalizedInterests.contains(catKey)) affinityScore += 4.0
        if (favoritedCategories.contains(catKey)) affinityScore += 3.5
        val sessionClicks = signals.categoryClickCounts[catKey] ?: 0
        if (sessionClicks > 0) {
            affinityScore += min(sessionClicks * 1.5, 4.5)
        }

        var qualityScore = 0.0
        if (item.organizerVerified) qualityScore += 2.0
        if (item.reviewCount > 0L && item.averageRating >= 4.0) {
            qualityScore += ((item.averageRating - 3.5) / 1.5).coerceIn(0.0, 1.0) * 2.0
        }

        // Controlled exploration jitter so pulling to refresh dynamically reshuffles close-tier items
        val explorationScore = deterministicExplorationUnit(item.id, signals.refreshSeed) * 4.5

        // Anti-fatigue: after user pulls to refresh, softly nudge already-opened items down
        // so fresh unseen activities rise to the top.
        val fatiguePenalty = if (signals.refreshCount > 0 && signals.viewedActivityIds.contains(item.id)) {
            -5.0
        } else {
            0.0
        }

        return priority1Score + priority2Score + priority3Score +
            affinityScore + qualityScore + explorationScore + fatiguePenalty
    }

    /**
     * Prevents more than 2 consecutive items of the exact same category on the main feed
     * while preserving overall score order as closely as possible.
     */
    private fun applyCategoryDiversity(sorted: List<ActivityItem>): List<ActivityItem> {
        val remaining = sorted.toMutableList()
        val result = ArrayList<ActivityItem>(sorted.size)

        while (remaining.isNotEmpty()) {
            val blockedCategory = if (result.size >= 2 &&
                result[result.size - 1].category.equals(result[result.size - 2].category, ignoreCase = true)
            ) {
                result[result.size - 1].category
            } else {
                null
            }

            val nextIndex = if (blockedCategory != null) {
                val diverseIdx = remaining.indexOfFirst {
                    !it.category.equals(blockedCategory, ignoreCase = true)
                }
                if (diverseIdx in 0..4) diverseIdx else 0
            } else {
                0
            }

            result.add(remaining.removeAt(nextIndex))
        }
        return result
    }

    /**
     * Deterministic pseudo-random number in [0.0, 1.0] derived from item ID and current refresh seed.
     */
    private fun deterministicExplorationUnit(itemId: String, seed: Long): Double {
        var h = seed xor (itemId.hashCode().toLong() * 0x9E3779B97F4A7C15UL.toLong())
        h = (h xor (h ushr 30)) * 0xBF58476D1CE4E5B9UL.toLong()
        h = (h xor (h ushr 27)) * 0x94D049BB133111EBUL.toLong()
        h = h xor (h ushr 31)
        val positive = h and Long.MAX_VALUE
        return (positive % 10_000L) / 10_000.0
    }
}
