package com.example.data.repository

import android.content.Context
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityCropPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class CommunityGalleryRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val storageFile = File(context.filesDir, "community_crop_posts.json")

    private val _posts = MutableStateFlow<List<CommunityCropPost>>(emptyList())
    val posts: StateFlow<List<CommunityCropPost>> = _posts.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        scope.launch {
            val loaded = loadFromDisk()
            if (loaded.isNotEmpty()) {
                _posts.value = loaded
            } else {
                val initial = getSampleCommunityPosts()
                _posts.value = initial
                saveToDisk(initial)
            }
        }
    }

    fun sharePhotoToCommunity(
        farmerName: String,
        village: String,
        district: String,
        cropName: String,
        variety: String,
        imageUrlOrUri: String,
        notes: String,
        grade: String,
        diagnosticBadge: String?,
        symptoms: String?,
        organicAdvice: String?,
        chemicalAdvice: String?
    ): CommunityCropPost {
        val newPost = CommunityCropPost(
            id = "user_post_${System.currentTimeMillis()}",
            farmerName = farmerName.ifBlank { "మీరు (రైతు / You)" },
            villageLocation = village.ifBlank { "స్థానిక గ్రామం" },
            district = district.ifBlank { "తెలంగాణ / ఆంధ్రప్రదేశ్" },
            cropName = cropName,
            cropVariety = variety.ifBlank { "స్థానిక రకం (Local)" },
            imageUrlOrUri = imageUrlOrUri,
            timestamp = System.currentTimeMillis(),
            timeAgo = "ఇప్పుడే / Just now",
            harvestGrade = grade,
            farmerNotes = notes,
            likesCount = 1,
            isLikedByMe = true,
            commentsCount = 0,
            isUserShared = true,
            diagnosticBadge = diagnosticBadge,
            symptomsSummary = symptoms,
            organicAdvice = organicAdvice,
            chemicalAdvice = chemicalAdvice,
            comments = emptyList()
        )

        val updatedList = listOf(newPost) + _posts.value
        _posts.value = updatedList
        scope.launch { saveToDisk(updatedList) }
        return newPost
    }

    fun toggleLike(postId: String) {
        val updatedList = _posts.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLikedByMe
                val newCount = if (newLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
                post.copy(isLikedByMe = newLiked, likesCount = newCount)
            } else {
                post
            }
        }
        _posts.value = updatedList
        scope.launch { saveToDisk(updatedList) }
    }

    fun addComment(postId: String, authorName: String, village: String, text: String) {
        if (text.isBlank()) return
        val updatedList = _posts.value.map { post ->
            if (post.id == postId) {
                val newComment = CommunityComment(
                    id = "comment_${System.currentTimeMillis()}",
                    authorName = authorName.ifBlank { "రైతు మిత్రుడు (Farmer)" },
                    authorVillage = village.ifBlank { "స్థానిక ప్రాంతం" },
                    text = text.trim(),
                    timestamp = System.currentTimeMillis(),
                    timeAgo = "ఇప్పుడే / Just now"
                )
                val newComments = post.comments + newComment
                post.copy(comments = newComments, commentsCount = newComments.size)
            } else {
                post
            }
        }
        _posts.value = updatedList
        scope.launch { saveToDisk(updatedList) }
    }

    fun deletePost(postId: String) {
        val updatedList = _posts.value.filterNot { it.id == postId }
        _posts.value = updatedList
        scope.launch { saveToDisk(updatedList) }
    }

    private fun saveToDisk(postsList: List<CommunityCropPost>) {
        try {
            val jsonArray = JSONArray()
            postsList.forEach { post ->
                val obj = JSONObject().apply {
                    put("id", post.id)
                    put("farmerName", post.farmerName)
                    put("villageLocation", post.villageLocation)
                    put("district", post.district)
                    put("cropName", post.cropName)
                    put("cropVariety", post.cropVariety)
                    put("imageUrlOrUri", post.imageUrlOrUri)
                    put("timestamp", post.timestamp)
                    put("timeAgo", post.timeAgo)
                    put("harvestGrade", post.harvestGrade)
                    put("farmerNotes", post.farmerNotes)
                    put("likesCount", post.likesCount)
                    put("isLikedByMe", post.isLikedByMe)
                    put("commentsCount", post.commentsCount)
                    put("isUserShared", post.isUserShared)
                    put("diagnosticBadge", post.diagnosticBadge ?: "")
                    put("symptomsSummary", post.symptomsSummary ?: "")
                    put("organicAdvice", post.organicAdvice ?: "")
                    put("chemicalAdvice", post.chemicalAdvice ?: "")

                    val commentsArr = JSONArray()
                    post.comments.forEach { c ->
                        val cObj = JSONObject().apply {
                            put("id", c.id)
                            put("authorName", c.authorName)
                            put("authorVillage", c.authorVillage)
                            put("text", c.text)
                            put("timestamp", c.timestamp)
                            put("timeAgo", c.timeAgo)
                        }
                        commentsArr.put(cObj)
                    }
                    put("comments", commentsArr)
                }
                jsonArray.put(obj)
            }
            storageFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            android.util.Log.e("CommunityGalleryRepo", "Failed to save posts to disk", e)
        }
    }

    private fun loadFromDisk(): List<CommunityCropPost> {
        return try {
            if (!storageFile.exists()) return emptyList()
            val text = storageFile.readText()
            if (text.isBlank()) return emptyList()
            val jsonArray = JSONArray(text)
            val list = mutableListOf<CommunityCropPost>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val commentsList = mutableListOf<CommunityComment>()
                val commentsArr = obj.optJSONArray("comments")
                if (commentsArr != null) {
                    for (j in 0 until commentsArr.length()) {
                        val cObj = commentsArr.getJSONObject(j)
                        commentsList.add(
                            CommunityComment(
                                id = cObj.optString("id"),
                                authorName = cObj.optString("authorName"),
                                authorVillage = cObj.optString("authorVillage"),
                                text = cObj.optString("text"),
                                timestamp = cObj.optLong("timestamp"),
                                timeAgo = cObj.optString("timeAgo")
                            )
                        )
                    }
                }

                list.add(
                    CommunityCropPost(
                        id = obj.optString("id"),
                        farmerName = obj.optString("farmerName"),
                        villageLocation = obj.optString("villageLocation"),
                        district = obj.optString("district"),
                        cropName = obj.optString("cropName"),
                        cropVariety = obj.optString("cropVariety"),
                        imageUrlOrUri = obj.optString("imageUrlOrUri"),
                        timestamp = obj.optLong("timestamp"),
                        timeAgo = obj.optString("timeAgo"),
                        harvestGrade = obj.optString("harvestGrade"),
                        farmerNotes = obj.optString("farmerNotes"),
                        likesCount = obj.optInt("likesCount"),
                        isLikedByMe = obj.optBoolean("isLikedByMe"),
                        commentsCount = commentsList.size.coerceAtLeast(obj.optInt("commentsCount")),
                        isUserShared = obj.optBoolean("isUserShared"),
                        diagnosticBadge = obj.optString("diagnosticBadge").ifBlank { null },
                        symptomsSummary = obj.optString("symptomsSummary").ifBlank { null },
                        organicAdvice = obj.optString("organicAdvice").ifBlank { null },
                        chemicalAdvice = obj.optString("chemicalAdvice").ifBlank { null },
                        comments = commentsList
                    )
                )
            }
            list
        } catch (e: Exception) {
            android.util.Log.e("CommunityGalleryRepo", "Failed to load posts from disk", e)
            emptyList()
        }
    }

    companion object {
        fun getSampleCommunityPosts(): List<CommunityCropPost> {
            val now = System.currentTimeMillis()
            return listOf(
                CommunityCropPost(
                    id = "community_post_1",
                    farmerName = "రామయ్య (Ramayya)",
                    villageLocation = "నర్సంపేట, ఖానాపూర్",
                    district = "వరంగల్ (Warangal)",
                    cropName = "మిర్చి (Chilli)",
                    cropVariety = "తేజా మిర్చి (Teja Variety)",
                    imageUrlOrUri = "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=800&q=80",
                    timestamp = now - 3_600_000 * 3,
                    timeAgo = "3 గంటల క్రితం / 3h ago",
                    harvestGrade = "A-Grade / ఎగుమతి నాణ్యత",
                    farmerNotes = "తేజా మిర్చి మొదటి కోత విజయవంతంగా పూర్తయింది. ఎకరాకు 26 క్వింటాళ్ల దిగుబడి వచ్చింది. తేమ 10% కంటే తక్కువగా ఎండబెట్టాము. వరంగల్ ఎనుమాముల మార్కెట్లో క్వింటాల్‌కు ₹21,800 పలికింది.",
                    likesCount = 48,
                    isLikedByMe = false,
                    commentsCount = 4,
                    isUserShared = false,
                    diagnosticBadge = "ఆరోగ్యవంతమైన పంట (Healthy)",
                    symptomsSummary = "కాయలపై మచ్చలు లేవు, మంచి ఎరుపు రంగు",
                    organicAdvice = "వేపనూనె 1500 ppm ప్రతి 12 రోజులకు ఒకసారి పిచికారీ చేయడం వల్ల రసం పీల్చే పురుగులు రాలేదు.",
                    chemicalAdvice = null,
                    comments = listOf(
                        CommunityComment("c1", "వెంకటేశ్వర్లు", "వర్ధన్నపేట", "మంచి దిగుబడి రామయ్య గారు! ఎండబెట్టడానికి టార్పాలిన్ వాడారా?"),
                        CommunityComment("c2", "రామయ్య", "నర్సంపేట", "అవును మిత్రమా, శుభ్రమైన సిమెంట్ కళ్ళం పై టార్పాలిన్ పరచి ఆరబెట్టాను.")
                    )
                ),
                CommunityCropPost(
                    id = "community_post_2",
                    farmerName = "సుబ్బారావు (Subba Rao)",
                    villageLocation = "కొల్లూరు, తెనాలి పరిధి",
                    district = "గుంటూరు (Guntur)",
                    cropName = "పత్తి (Cotton)",
                    cropVariety = "బీటీ కాటన్ (RCH-659)",
                    imageUrlOrUri = "https://images.unsplash.com/photo-1606041008023-472dfb5e530f?w=800&q=80",
                    timestamp = now - 3_600_000 * 7,
                    timeAgo = "7 గంటల క్రితం / 7h ago",
                    harvestGrade = "ఉత్తమ నాణ్యత / A-Grade",
                    farmerNotes = "గులాబీ రంగు పురుగు నివారణకు ప్రారంభం నుంచే ఎకరాకు 6 లింగాకర్షక బుట్టలు (Pheromone traps) పెట్టాము. తెల్లటి నాణ్యమైన దూది వచ్చింది, తేమ శాతం చాలా తక్కువ.",
                    likesCount = 63,
                    isLikedByMe = true,
                    commentsCount = 3,
                    isUserShared = false,
                    diagnosticBadge = "పురుగు నియంత్రణ విజయం (PBW Free)",
                    symptomsSummary = "కాయ తొలచు పురుగు దాడి నియంత్రించబడింది",
                    organicAdvice = "ట్రైకోగ్రామా కార్డులు ఎకరాకు 2 సార్లు అమర్చడం మంచి ఫలితం ఇచ్చింది.",
                    chemicalAdvice = "తీవ్రత ఉన్నప్పుడు ప్రొఫెనోఫాస్ 2ml/లీటరు పిచికారీ చేయవచ్చు.",
                    comments = listOf(
                        CommunityComment("c3", "శ్రీధర్ రెడ్డి", "సత్తెనపల్లి", "గుంటూరు యార్డులో ధర ఎంత పలికింది?")
                    )
                ),
                CommunityCropPost(
                    id = "community_post_3",
                    farmerName = "వెంకటేశ్వరరావు (Venkateswara Rao)",
                    villageLocation = "పాలకొల్లు, నరసాపురం",
                    district = "పశ్చిమ గోదావరి (West Godavari)",
                    cropName = "వరి (Paddy)",
                    cropVariety = "BPT-5204 (సాంబా మసూరి)",
                    imageUrlOrUri = "https://images.unsplash.com/photo-1536657464919-892534f60d6e?w=800&q=80",
                    timestamp = now - 3_600_000 * 18,
                    timeAgo = "నిన్న / Yesterday",
                    harvestGrade = "కోతకు సిద్ధం / Ready to Harvest",
                    farmerNotes = "గోదావరి డెల్టాలో సాంబా మసూరి వరి కోతకు సిద్ధంగా ఉంది. గింజ మంచి మెరుపుతో నిండుగా వచ్చింది. అగ్గి తెగులు (Blast) రాకుండా సమతుల్య పొటాష్ ఎరువులు ఉపయోగించాము.",
                    likesCount = 92,
                    isLikedByMe = false,
                    commentsCount = 5,
                    isUserShared = false,
                    diagnosticBadge = "అగ్గి తెగులు నివారించబడింది (Blast Free)",
                    symptomsSummary = "ఆకులపై అగ్గి తెగులు మచ్చలు లేవు",
                    organicAdvice = "జీవామృతం 200 లీటర్లు నీటితో పాటు పారించడం వల్ల వేరు వ్యవస్థ దృఢంగా ఉంది.",
                    chemicalAdvice = "ట్రైసైక్లాజోల్ 0.6 గ్రా/లీటరు పిచికారీ ముందస్తుగా చేశాము."
                ),
                CommunityCropPost(
                    id = "community_post_4",
                    farmerName = "లక్ష్మీదేవి (Lakshmi Devi)",
                    villageLocation = "కురబలకోట, మదనపల్లె",
                    district = "అన్నమయ్య - చిత్తూరు (Annamayya)",
                    cropName = "టమాటా (Tomato)",
                    cropVariety = "సాహో హైబ్రిడ్ టమాటా",
                    imageUrlOrUri = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=800&q=80",
                    timestamp = now - 3_600_000 * 24,
                    timeAgo = "నిన్న / Yesterday",
                    harvestGrade = "మొదటి గ్రేడ్ / A-Grade",
                    farmerNotes = "మదనపల్లె మార్కెట్‌కు 75 క్రేట్ల తాజా గట్టి టమాటాలు పంపాము. ఆకుముడుత నివారణకు పసుపు రంగు జిగురు అట్టలు ఎకరాకు 20 చొప్పున పెట్టడం వల్ల తెల్లదోమ నియంత్రణలోకి వచ్చింది.",
                    likesCount = 54,
                    isLikedByMe = false,
                    commentsCount = 2,
                    isUserShared = false,
                    diagnosticBadge = "వైరస్ ముడుత నియంత్రణ (Controlled)",
                    symptomsSummary = "ఆకులు ముడుచుకోకుండా పచ్చగా ఉన్నాయి",
                    organicAdvice = "వేప గింజల కషాయం 5% పిచికారీ ఉత్తమం.",
                    chemicalAdvice = "డైఫెన్‌థియురాన్ 1.25 గ్రా/లీటరు నీటిలో పిచికారీ చేయవచ్చు."
                ),
                CommunityCropPost(
                    id = "community_post_5",
                    farmerName = "శ్రీనివాస్ రెడ్డి (Srinivas Reddy)",
                    villageLocation = "ఆర్మూర్, బోధన్",
                    district = "నిజామాబాద్ (Nizamabad)",
                    cropName = "పసుపు (Turmeric)",
                    cropVariety = "సేలం & ప్రగతి పసుపు",
                    imageUrlOrUri = "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=800&q=80",
                    timestamp = now - 3_600_000 * 36,
                    timeAgo = "2 రోజుల క్రితం / 2d ago",
                    harvestGrade = "A-Grade / అధిక కుర్కుమిన్",
                    farmerNotes = "దుంప కుళ్లు తెగులు రాకుండా ట్రైకోడెర్మా విరిడేతో విత్తన శుద్ధి చేశాము. పసుపు కొమ్ములు లావుగా, రంగు అద్భుతంగా వచ్చాయి. మార్కెట్లో మంచి ధర ఆశిస్తున్నాము.",
                    likesCount = 81,
                    isLikedByMe = true,
                    commentsCount = 6,
                    isUserShared = false,
                    diagnosticBadge = "దుంప కుళ్లు నివారించబడింది",
                    symptomsSummary = "దుంపలు దృఢంగా ఉన్నాయి, కుళ్లు లేదు",
                    organicAdvice = "పశువుల ఎరువులో ట్రైకోడెర్మా కలిపి ఆఖరి దుక్కిలో వేయాలి.",
                    chemicalAdvice = "మెటలాక్సిల్ + మాంకోజెబ్ 2 గ్రా/లీటరు పిచికారీ చేయాలి."
                ),
                CommunityCropPost(
                    id = "community_post_6",
                    farmerName = "మల్లేష్ గౌడ్ (Mallesh Goud)",
                    villageLocation = "ఎమ్మిగనూరు, ఆదోని",
                    district = "కర్నూలు (Kurnool)",
                    cropName = "వేరుశనగ (Groundnut)",
                    cropVariety = "లేపాక్షి-1812 (Kadiri Lepakshi)",
                    imageUrlOrUri = "https://images.unsplash.com/photo-1567406893414-76b7b1e5a7a5?w=800&q=80",
                    timestamp = now - 3_600_000 * 48,
                    timeAgo = "2 రోజుల క్రితం / 2d ago",
                    harvestGrade = "మంచి నూనె శాతం / B-Grade",
                    farmerNotes = "టిక్కా ఆకుమచ్చ తెగులు తొలినాళ్లలోనే గుర్తించి కార్బండైజిమ్ పిచికారీ చేశాము. పంట విజయవంతంగా కోలుకొని కాయ సైజు బాగా వచ్చింది. తోటి రైతులకు సలహా: మచ్చలు కనిపించగానే నిర్లక్ష్యం చేయకండి.",
                    likesCount = 39,
                    isLikedByMe = false,
                    commentsCount = 3,
                    isUserShared = false,
                    diagnosticBadge = "తెగులు నుండి కోలుకుంది (Recovered)",
                    symptomsSummary = "టిక్కా ఆకుమచ్చలు నియంత్రణలోకి వచ్చాయి",
                    organicAdvice = "దశపర్ణి కషాయం స్ప్రే చేయండి.",
                    chemicalAdvice = "కార్బండైజిమ్ 1 గ్రా లేదా హెక్సాకొనాజోల్ 2ml/లీ."
                )
            )
        }
    }
}
