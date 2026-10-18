package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityCropPost
import com.example.ui.components.AudioReadoutButton
import com.example.ui.theme.AgriGreen
import com.example.ui.theme.HarvestGold
import com.example.ui.viewmodel.KisanViewModel

enum class CommunityGalleryFilter(val labelTe: String, val labelEn: String) {
    ALL("అందరి రైతులు", "All Farmers"),
    MY_POSTS("నా ఫోటోలు", "My Posts"),
    TOP_QUALITY("ఉత్తమ పంటలు", "A-Grade Yield"),
    PEST_REMEDY("తెగుళ్ల నివారణ", "Pest & Remedy")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityGalleryScreen(
    viewModel: KisanViewModel,
    modifier: Modifier = Modifier,
    onNavigateToUploadDoctor: () -> Unit = {}
) {
    val language by viewModel.language.collectAsState()
    val posts by viewModel.communityPosts.collectAsState()
    val isDataSaver by viewModel.isDataSaver.collectAsState()

    var selectedFilter by remember { mutableStateOf(CommunityGalleryFilter.ALL) }
    var selectedCropFilter by remember { mutableStateOf("అన్నీ (All)") }
    var searchQuery by remember { mutableStateOf("") }
    var showShareModal by remember { mutableStateOf(false) }
    var activeDetailsPost by remember { mutableStateOf<CommunityCropPost?>(null) }

    // Quick Crop categories
    val cropCategories = listOf(
        "అన్నీ (All)",
        "మిర్చి (Chilli)",
        "పత్తి (Cotton)",
        "వరి (Paddy)",
        "టమాటా (Tomato)",
        "పసుపు (Turmeric)",
        "వేరుశనగ (Groundnut)",
        "మొక్కజొన్న (Maize)"
    )

    // Filter logic
    val filteredPosts = posts.filter { post ->
        val matchesCategory = when (selectedFilter) {
            CommunityGalleryFilter.ALL -> true
            CommunityGalleryFilter.MY_POSTS -> post.isUserShared
            CommunityGalleryFilter.TOP_QUALITY -> post.harvestGrade.contains("A-Grade", ignoreCase = true) || post.harvestGrade.contains("ఉత్తమ", ignoreCase = true)
            CommunityGalleryFilter.PEST_REMEDY -> !post.diagnosticBadge.isNullOrBlank() || !post.organicAdvice.isNullOrBlank()
        }

        val matchesCrop = if (selectedCropFilter == "అన్నీ (All)") {
            true
        } else {
            val key = selectedCropFilter.substringBefore(" ").trim()
            post.cropName.contains(key, ignoreCase = true) || post.cropVariety.contains(key, ignoreCase = true)
        }

        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            post.cropName.contains(searchQuery, ignoreCase = true) ||
            post.cropVariety.contains(searchQuery, ignoreCase = true) ||
            post.farmerName.contains(searchQuery, ignoreCase = true) ||
            post.villageLocation.contains(searchQuery, ignoreCase = true) ||
            post.district.contains(searchQuery, ignoreCase = true)
        }

        matchesCategory && matchesCrop && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("community_gallery_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                // Banner illustration
                Image(
                    painter = painterResource(id = R.drawable.kisan_gallery_hero),
                    contentDescription = "Farmers with Crop Harvest",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for readable text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Banner Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("gallery_title", language),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = AppStrings.get("gallery_subtitle", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        AudioReadoutButton(
                            onClick = {
                                val spoken = if (language == AppLanguage.TELUGU) {
                                    "రైతుల కమ్యూనిటీ గ్యాలరీకి స్వాగతం. ఇక్కడ తెలంగాణ మరియు ఆంధ్రప్రదేశ్ రైతులు పంచుకున్న పంటల ఫోటోలు, తెగుళ్ల నివారణ అనుభవాలు మరియు సలహాలు చూడవచ్చు. మీరు కూడా మీ పంట ఫోటోను గ్యాలరీలో పంచుకోవచ్చు."
                                } else {
                                    "Welcome to Kisan Community Gallery. Browse crop harvest photos, pest treatment experiences, and peer advice shared by farmers across districts. You can also share your own crop photos."
                                }
                                viewModel.readAloud(spoken)
                            },
                            language = language
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Button inside hero: "+ Share Your Crop Photo"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showShareModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("share_crop_photo_button")
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("share_crop_photo", language),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onNavigateToUploadDoctor,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.5.dp, Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("open_crop_doctor_button")
                        ) {
                            Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.TELUGU) "పంట డాక్టర్" else "Crop Doctor",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Stats Banner: Community Pulse
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AgriGreen.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, AgriGreen.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CommunityStatPill(
                        icon = "🌾",
                        count = "${posts.size}",
                        label = if (language == AppLanguage.TELUGU) "పంట ఫోటోలు" else "Crop Photos"
                    )
                    CommunityStatPill(
                        icon = "📍",
                        count = "8",
                        label = if (language == AppLanguage.TELUGU) "వ్యవసాయ జిల్లాలు" else "Districts"
                    )
                    CommunityStatPill(
                        icon = "🛡️",
                        count = "100%",
                        label = if (language == AppLanguage.TELUGU) "రైతు సలహాలు" else "Verified Tips"
                    )
                    CommunityStatPill(
                        icon = "⚡",
                        count = if (isDataSaver) "210 KB" else "HD",
                        label = if (language == AppLanguage.TELUGU) "డేటా ఆదా" else "Data Saver"
                    )
                }
            }
        }

        // Category Filter Tabs
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SecondaryTabRow(
                    selectedTabIndex = selectedFilter.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = AgriGreen,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    CommunityGalleryFilter.values().forEach { filter ->
                        val selected = selectedFilter == filter
                        Tab(
                            selected = selected,
                            onClick = { selectedFilter = filter },
                            text = {
                                Text(
                                    text = if (language == AppLanguage.TELUGU) filter.labelTe else filter.labelEn,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) AgriGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        }

        // Horizontal Crop Chips Scroll
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cropCategories.forEach { cropLabel ->
                    val isSelected = selectedCropFilter == cropLabel
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCropFilter = cropLabel },
                        label = {
                            Text(
                                text = cropLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgriGreen,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AgriGreen else MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }
        }

        // Search Bar for Gallery
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("gallery_search_input"),
                placeholder = {
                    Text(
                        text = if (language == AppLanguage.TELUGU) "రైతు పేరు, గ్రామం లేదా పంట వెతకండి..." else "Search by farmer, village or crop...",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AgriGreen)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Empty state or Posts list
        if (filteredPosts.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🌱", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.TELUGU) "ఎలాంటి పంట ఫోటోలు కనుగొనబడలేదు" else "No crop photos found for this filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.TELUGU)
                                "మొదటి ఫోటోను మీరే పంచుకోండి! ఇతర రైతులకు మీ పంట అనుభవం సహాయపడుతుంది."
                            else
                                "Be the first to share a photo! Your experience helps fellow farmers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showShareModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (language == AppLanguage.TELUGU) "ఇప్పుడే పంచుకోండి" else "Share Photo Now")
                        }
                    }
                }
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                CommunityPostCard(
                    post = post,
                    language = language,
                    onLikeClick = { viewModel.toggleCommunityPostLike(post.id) },
                    onOpenDetails = { activeDetailsPost = post },
                    onReadAloud = {
                        val textToRead = "${post.farmerName}, ${post.villageLocation}. పంట: ${post.cropName}, రకం: ${post.cropVariety}. రైతు అనుభవం: ${post.farmerNotes}."
                        viewModel.readAloudInTelugu(textToRead)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Modal: Share Crop Photo to Community
    if (showShareModal) {
        ShareCropPhotoDialog(
            language = language,
            onDismiss = { showShareModal = false },
            onShare = { farmer, village, district, crop, variety, uriString, notes, grade, attachDiag ->
                viewModel.shareCropPhotoToCommunity(
                    farmerName = farmer,
                    village = village,
                    district = district,
                    cropName = crop,
                    variety = variety,
                    imageUrlOrUri = uriString,
                    notes = notes,
                    grade = grade,
                    attachDiagnosis = attachDiag
                )
                showShareModal = false
            }
        )
    }

    // Modal: Details & Comments Dialog
    if (activeDetailsPost != null) {
        CommunityPostDetailsDialog(
            post = activeDetailsPost!!,
            language = language,
            onDismiss = { activeDetailsPost = null },
            onLike = {
                viewModel.toggleCommunityPostLike(activeDetailsPost!!.id)
                // update local state
                activeDetailsPost = posts.firstOrNull { it.id == activeDetailsPost!!.id }
            },
            onAddComment = { commentText, authorName, authorVillage ->
                viewModel.addCommunityComment(
                    postId = activeDetailsPost!!.id,
                    authorName = authorName,
                    village = authorVillage,
                    text = commentText
                )
                activeDetailsPost = posts.firstOrNull { it.id == activeDetailsPost!!.id }
            },
            onReadAloud = {
                val post = activeDetailsPost!!
                val spoken = "${post.farmerName}, గ్రామం ${post.villageLocation}. పంట ${post.cropName}. రైతు సలహా: ${post.farmerNotes}. ${post.organicAdvice ?: ""}"
                viewModel.readAloudInTelugu(spoken)
            }
        )
    }
}

@Composable
fun CommunityStatPill(
    icon: String,
    count: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AgriGreen
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityCropPost,
    language: AppLanguage,
    onLikeClick: () -> Unit,
    onOpenDetails: () -> Unit,
    onReadAloud: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heartColor by animateColorAsState(
        targetValue = if (post.isLikedByMe) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "heart_color"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.5.dp,
            if (post.isUserShared) AgriGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
            .testTag("community_post_${post.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Farmer Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Farmer Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (post.isUserShared) AgriGreen else HarvestGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.farmerName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.farmerName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (post.isUserShared) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = AgriGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.TELUGU) "మీరు" else "You",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${post.villageLocation} • ${post.district}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Time ago pill
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = post.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Crop Name & Harvest Grade Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = AgriGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌱", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.cropName} - ${post.cropVariety}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AgriGreen
                        )
                    }
                }

                Surface(
                    color = HarvestGold.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = post.harvestGrade,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Crop Photo Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = post.imageUrlOrUri,
                    contentDescription = "${post.cropName} Harvest Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Optional Diagnosis badge overlay on top right of photo
                if (!post.diagnosticBadge.isNullOrBlank()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔬", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.diagnosticBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Farmer Notes Text
            Text(
                text = post.farmerNotes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Organic Advice Highlight if present
            if (!post.organicAdvice.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = AgriGreen.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AgriGreen.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                        Text("🌿", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.organicAdvice,
                            style = MaterialTheme.typography.labelSmall,
                            color = AgriGreen,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            // Action Row: Like, Comments, Voice Audio, Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like / Helpful Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLikeClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = heartColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = heartColor
                    )
                }

                // Comment count button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenDetails() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.commentsCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Audio Readout (Telugu Speech)
                IconButton(
                    onClick = onReadAloud,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Listen Post",
                        tint = AgriGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Full Details CTA Button
                OutlinedButton(
                    onClick = onOpenDetails,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriGreen),
                    border = BorderStroke(1.dp, AgriGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.TELUGU) "వివరాలు" else "Details",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ShareCropPhotoDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onShare: (
        farmer: String,
        village: String,
        district: String,
        crop: String,
        variety: String,
        uriString: String,
        notes: String,
        grade: String,
        attachDiag: Boolean
    ) -> Unit
) {
    var farmerName by remember { mutableStateOf("మీరు (రైతు)") }
    var villageName by remember { mutableStateOf("వరంగల్ రూరల్") }
    var districtName by remember { mutableStateOf("వరంగల్ (Warangal)") }
    var selectedCrop by remember { mutableStateOf("మిర్చి (Chilli)") }
    var cropVariety by remember { mutableStateOf("తేజా రకం (Teja)") }
    var farmerNotes by remember { mutableStateOf("పంట మొదటి కోత పూర్తయింది. నాణ్యత బాగుంది, తేమ శాతం తక్కువగా ఉండేలా జాగ్రత్త పడ్డాము.") }
    var selectedGrade by remember { mutableStateOf("A-Grade / ఎగుమతి నాణ్యత") }
    var attachDiagnosis by remember { mutableStateOf(true) }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedImageUri = uri
        }
    }

    val sampleCrops = listOf("మిర్చి (Chilli)", "పత్తి (Cotton)", "వరి (Paddy)", "టమాటా (Tomato)", "పసుపు (Turmeric)", "వేరుశనగ (Groundnut)")
    val sampleGrades = listOf("A-Grade / ఎగుమతి నాణ్యత", "B-Grade / మంచి నాణ్యత", "కోతకు సిద్ధం", "వ్యాధి నివారణ దశ")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AgriGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.TELUGU) "రైతుల గ్యాలరీలో మీ పంటను పంచుకోండి" else "Share Your Crop Photo to Community",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (language == AppLanguage.TELUGU)
                            "మీ పంట ఫోటో, అనుభవం మరియు దిగుబడి నాణ్యతను ఇతర రైతులతో పంచుకోండి."
                        else
                            "Share your crop photo, field observations and harvest quality with fellow farmers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Photo Selection Section
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, AgriGreen.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        if (pickedImageUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                AsyncImage(
                                    model = pickedImageUri,
                                    contentDescription = "Selected Crop Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.TELUGU) "మార్చండి" else "Change",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = AgriGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.TELUGU) "ఫోటో ఎంచుకోండి (గ్యాలరీ / కెమెరా)" else "Select Crop Photo (Gallery / Camera)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreen
                                )
                                Text(
                                    text = if (language == AppLanguage.TELUGU) "డేటా ఆదా మోడ్‌లో కుదించబడుతుంది" else "Optimized for rural low-bandwidth",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = farmerName,
                        onValueChange = { farmerName = it },
                        label = { Text(if (language == AppLanguage.TELUGU) "రైతు పేరు" else "Farmer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = villageName,
                            onValueChange = { villageName = it },
                            label = { Text(if (language == AppLanguage.TELUGU) "గ్రామం" else "Village") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = districtName,
                            onValueChange = { districtName = it },
                            label = { Text(if (language == AppLanguage.TELUGU) "జిల్లా" else "District") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // Crop Selection
                item {
                    Text(
                        text = if (language == AppLanguage.TELUGU) "పంటను ఎంచుకోండి:" else "Select Crop:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sampleCrops.forEach { crop ->
                            FilterChip(
                                selected = selectedCrop == crop,
                                onClick = { selectedCrop = crop },
                                label = { Text(crop.substringBefore(" "), style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgriGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = cropVariety,
                        onValueChange = { cropVariety = it },
                        label = { Text(if (language == AppLanguage.TELUGU) "పంట రకం / విత్తనం" else "Crop Variety / Seed") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = farmerNotes,
                        onValueChange = { farmerNotes = it },
                        label = { Text(if (language == AppLanguage.TELUGU) "మీ అనుభవం / సలహా" else "Field Notes & Farmer Advice") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }

                // Harvest Grade Selection
                item {
                    Text(
                        text = if (language == AppLanguage.TELUGU) "నాణ్యత గ్రేడ్:" else "Harvest Grade:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sampleGrades.forEach { grade ->
                            FilterChip(
                                selected = selectedGrade == grade,
                                onClick = { selectedGrade = grade },
                                label = { Text(grade, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HarvestGold,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalUri = pickedImageUri?.toString() ?: "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=800&q=80"
                    onShare(
                        farmerName,
                        villageName,
                        districtName,
                        selectedCrop,
                        cropVariety,
                        finalUri,
                        farmerNotes,
                        selectedGrade,
                        attachDiagnosis
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreen),
                modifier = Modifier.testTag("confirm_share_crop_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == AppLanguage.TELUGU) "గ్యాలరీలో పంచుకోండి" else "Share to Gallery",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (language == AppLanguage.TELUGU) "రద్దు చేయండి" else "Cancel")
            }
        }
    )
}

@Composable
fun CommunityPostDetailsDialog(
    post: CommunityCropPost,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onLike: () -> Unit,
    onAddComment: (comment: String, author: String, village: String) -> Unit,
    onReadAloud: () -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("మీరు (రైతు)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AgriGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.farmerName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(post.farmerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${post.villageLocation} • ${post.district}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onReadAloud) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Listen", tint = AgriGreen)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // High-res photo preview
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = post.imageUrlOrUri,
                            contentDescription = "${post.cropName} Full View",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Crop & Grade Badges
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = AgriGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${post.cropName} (${post.cropVariety})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgriGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = HarvestGold.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = post.harvestGrade,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Farmer Notes
                item {
                    Text(
                        text = if (language == AppLanguage.TELUGU) "రైతు వివరణ & అనుభవం:" else "Farmer Field Experience:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.farmerNotes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Pathology & Diagnostic Advice if present
                if (!post.diagnosticBadge.isNullOrBlank() || !post.organicAdvice.isNullOrBlank() || !post.chemicalAdvice.isNullOrBlank()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AgriGreen.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, AgriGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = AgriGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.TELUGU) "తెగులు గుర్తింపు & నివారణ సలహాలు" else "Pathology & Remedy Guidance",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreen
                                    )
                                }

                                if (!post.diagnosticBadge.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "నిర్ధారణ: ${post.diagnosticBadge}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (!post.organicAdvice.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "🌿 సేంద్రీయ పరిష్కారం: ${post.organicAdvice}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AgriGreen
                                    )
                                }

                                if (!post.chemicalAdvice.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🧪 రసాయన మందు: ${post.chemicalAdvice}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Like counter row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onLike,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (post.isLikedByMe) Color(0xFFE91E63) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (post.isLikedByMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (post.isLikedByMe) "మెచ్చారు (${post.likesCount})" else "ఉపయోగపడింది (${post.likesCount})",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Text(
                            text = "${post.comments.size} సలహాలు / వ్యాఖ్యలు",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Existing Comments
                item {
                    Text(
                        text = if (language == AppLanguage.TELUGU) "రైతుల ప్రశ్నలు & సలహాలు:" else "Farmer Community Comments:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (post.comments.isEmpty()) {
                    item {
                        Text(
                            text = if (language == AppLanguage.TELUGU) "ఇంకా వ్యాఖ్యలు లేవు. మొదటి సలహా మీరే ఇవ్వండి!" else "No comments yet. Be the first to advise!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(post.comments) { comment ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${comment.authorName} (${comment.authorVillage})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriGreen
                                    )
                                    Text(
                                        text = comment.timeAgo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Write Comment Field
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = {
                            Text(
                                text = if (language == AppLanguage.TELUGU) "రైతుకు మీ సలహా లేదా ప్రశ్న రాయండి..." else "Write advice or ask a question...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newCommentText.isNotBlank()) {
                                        onAddComment(newCommentText, authorName, "స్థానిక ప్రాంతం")
                                        newCommentText = ""
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = AgriGreen)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreen)
            ) {
                Text(if (language == AppLanguage.TELUGU) "పూర్తయింది" else "Done")
            }
        }
    )
}
