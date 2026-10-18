package com.example.data.model

data class CommunityComment(
    val id: String,
    val authorName: String,
    val authorVillage: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timeAgo: String = "ఇప్పుడే / Just now"
)

data class CommunityCropPost(
    val id: String,
    val farmerName: String,
    val villageLocation: String,
    val district: String,
    val cropName: String,
    val cropVariety: String,
    val imageUrlOrUri: String,
    val timestamp: Long,
    val timeAgo: String,
    val harvestGrade: String,
    val farmerNotes: String,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val commentsCount: Int = 0,
    val isUserShared: Boolean = false,
    val diagnosticBadge: String? = null,
    val symptomsSummary: String? = null,
    val organicAdvice: String? = null,
    val chemicalAdvice: String? = null,
    val comments: List<CommunityComment> = emptyList()
)
