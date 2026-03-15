package org.example.project

data class AccessibilityFeature(
    val id: String,
    val emoji: String,
    val label: String,
    var upvotes: Int = 0,
    var downvotes: Int = 0
) {
    val total get() = upvotes + downvotes
    val confidence get() = when {
        total < 5 -> "❓ Unverified"
        upvotes.toFloat() / total >= 0.8f -> "✅ Confirmed"
        upvotes.toFloat() / total >= 0.5f -> "⚠️ Disputed"
        else -> "❌ Not available"
    }
    val confidenceColor get() = when {
        total < 5 -> "gray"
        upvotes.toFloat() / total >= 0.8f -> "green"
        upvotes.toFloat() / total >= 0.5f -> "orange"
        else -> "red"
    }
}

data class POI(
    val id: Int,
    val name: String,
    val category: String,
    val emoji: String,
    val lat: Double,
    val lon: Double,
    val features: MutableList<AccessibilityFeature>
)

fun defaultFeatures() = mutableListOf(
    AccessibilityFeature("wheelchair", "🦽", "Wheelchair accessible entrance"),
    AccessibilityFeature("hearing", "🔇", "Hearing loop available"),
    AccessibilityFeature("visual", "👁️", "Tactile paving / guide paths"),
    AccessibilityFeature("neurodivergent", "🧠", "Quiet / low sensory environment"),
    AccessibilityFeature("toilet", "🚻", "Accessible toilet"),
    AccessibilityFeature("parking", "🅿️", "Accessible parking nearby")
)

object POIStore {
    val pois = mutableListOf(
        POI(1, "Iulius Mall Timișoara", "🛍️ Mall", "🛍️", 45.7752, 21.2384, defaultFeatures()),
        POI(2, "Piața Victoriei", "📍 Square", "📍", 45.7494, 21.2272, defaultFeatures()),
        POI(3, "Café Wien", "☕ Café", "☕", 45.7489, 21.2261, defaultFeatures()),
        POI(4, "Restaurant Timișoreana", "🍽️ Restaurant", "🍽️", 45.7501, 21.2298, defaultFeatures()),
        POI(5, "Farmacia Catena", "💊 Pharmacy", "💊", 45.7478, 21.2301, defaultFeatures()),
        POI(6, "Muzeul Național al Banatului", "🏛️ Museum", "🏛️", 45.7573, 21.2297, defaultFeatures()),
        POI(7, "Parc Rozelor", "🌳 Park", "🌳", 45.7443, 21.2198, defaultFeatures()),
        POI(8, "Spitalul Județean Timișoara", "🏥 Hospital", "🏥", 45.7389, 21.2423, defaultFeatures()),
        POI(9, "Piața Unirii", "📍 Square", "📍", 45.7576, 21.2269, defaultFeatures()),
        POI(10, "Kaufland Timișoara", "🛒 Supermarket", "🛒", 45.7421, 21.2501, defaultFeatures())
    )

    // Track which features the current user has already voted on
    private val userVotes = mutableMapOf<String, Boolean>() // key: "poiId_featureId", value: true=up, false=down

    fun vote(poiId: Int, featureId: String, isUpvote: Boolean) {
        val poi = pois.find { it.id == poiId } ?: return
        val feature = poi.features.find { it.id == featureId } ?: return
        val voteKey = "${poiId}_${featureId}"

        // If already voted the same way, remove vote (toggle off)
        if (userVotes[voteKey] == isUpvote) {
            if (isUpvote) feature.upvotes-- else feature.downvotes--
            userVotes.remove(voteKey)
            return
        }

        // If switching vote, remove old vote first
        if (userVotes.containsKey(voteKey)) {
            if (userVotes[voteKey] == true) feature.upvotes-- else feature.downvotes--
        }

        // Add new vote
        if (isUpvote) feature.upvotes++ else feature.downvotes++
        userVotes[voteKey] = isUpvote
    }

    fun getUserVote(poiId: Int, featureId: String): Boolean? {
        return userVotes["${poiId}_${featureId}"]
    }
}