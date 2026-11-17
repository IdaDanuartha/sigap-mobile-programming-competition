package id.sigap.app.feature.learn

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.User
import com.composables.icons.lucide.Zap
import id.sigap.app.R
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.data.local.seeded.SeededTrends
import id.sigap.app.domain.learn.TrendArticle
import id.sigap.app.domain.model.ScamCategory

/**
 * S10A Belajar: Pusat Edukasi & Modus Penipuan Terkini matching Mockup Image 2.
 */
@Composable
fun LearnScreen(
    onOpenArticle: (String) -> Unit,
    onOpenQuiz: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }
    var showOnlyBookmarks by remember { mutableStateOf(false) }
    var bookmarkedIds by remember { mutableStateOf(setOf("fake_courier_photo", "bank_account_takeover")) }

    val filterOptions = listOf("Semua", "Modus APK", "Kurir & Paket", "Tilang Elektronik", "Kerja Online")

    // Filter articles based on category, search query, and bookmark state
    val allArticles = SeededTrends.ARTICLES
    val filteredArticles = allArticles.filter { article ->
        val matchesCategory = when (selectedFilter) {
            "Semua" -> true
            "Modus APK" -> article.category == ScamCategory.FAKE_INVITATION || article.titleId.contains("APK", ignoreCase = true)
            "Kurir & Paket" -> article.category == ScamCategory.FAKE_COURIER || article.titleId.contains("Kurir", ignoreCase = true)
            "Tilang Elektronik" -> article.category == ScamCategory.AUTHORITY_IMPERSONATION || article.titleId.contains("Tilang", ignoreCase = true)
            "Kerja Online" -> article.category == ScamCategory.JOB_SCAM || article.titleId.contains("Kerja", ignoreCase = true)
            else -> true
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            article.titleId.contains(searchQuery, ignoreCase = true) ||
                article.summaryId.contains(searchQuery, ignoreCase = true)
        }
        val matchesBookmark = if (showOnlyBookmarks) article.id in bookmarkedIds else true
        matchesCategory && matchesSearch && matchesBookmark
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Top App Bar: Shield icon, "Belajar", Notification bell, Profile
        LearnTopBar(
            onOpenNotifications = onOpenNotifications,
            onOpenProfile = onOpenProfile,
        )

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Search Input with Bookmark Icon
            SearchBox(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                isBookmarkedOnly = showOnlyBookmarks,
                onToggleBookmarks = { showOnlyBookmarks = !showOnlyBookmarks },
            )

            // Lanskap Ancaman Siber Card (Real-time OJK stats)
            ThreatLandscapeCard()

            // Horizontal Filter Chips
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedFilter = filter },
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            // Section Header: Artikel Populer + Lihat Kurikulum
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Artikel Populer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Lihat Kurikulum",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { /* Opens full curriculum */ },
                )
            }

            // Article List Cards
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (filteredArticles.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    ) {
                        Text(
                            text = "Tidak ada artikel yang cocok dengan pencarian Anda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SigapTheme.colors.textMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                } else {
                    filteredArticles.forEach { article ->
                        val badge = when (article.category) {
                            ScamCategory.FAKE_INVITATION -> "Modus APK"
                            ScamCategory.FAKE_COURIER -> "Kurir & Paket"
                            ScamCategory.AUTHORITY_IMPERSONATION -> "Tilang Elektronik"
                            ScamCategory.JOB_SCAM -> "Kerja Online"
                            else -> "Penipuan Siber"
                        }
                        val imageRes = when (article.id) {
                            "apk_wedding_invitation" -> R.drawable.trend_apk_scam
                            "fake_courier_photo" -> R.drawable.trend_apk_scam
                            "online_shopping_marketplace" -> R.drawable.sample_phishing_proof
                            "traffic_ticket_authority" -> R.drawable.sample_phishing_proof
                            "part_time_job_commission" -> R.drawable.sample_phishing_proof
                            else -> R.drawable.trend_apk_scam
                        }
                        PopularArticleCard(
                            categoryBadge = badge,
                            isTrending = article.id == "fake_courier_photo",
                            title = article.titleId,
                            description = article.summaryId,
                            readTime = "${article.readMinutes} menit baca",
                            imageRes = imageRes,
                            onClick = { onOpenArticle(article.id) },
                        )
                    }
                }
            }

            // Bottom Banner: TES KETANGKASAN
            AgilityQuizBanner(onPlay = onOpenQuiz)

            Spacer(Modifier.height(Spacing.Section))
        }
    }
}

@Composable
private fun LearnTopBar(
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(Color(0xFFF9F1E4), RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_mark),
                    contentDescription = "Logo Sigap",
                    modifier = Modifier.size(26.dp),
                )
            }
            Text(
                text = "Belajar",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconButton(onClick = onOpenNotifications) {
                Icon(
                    Lucide.Bell,
                    contentDescription = "Pusat Pemberitahuan",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenProfile)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.User,
                    contentDescription = "Profil",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    isBookmarkedOnly: Boolean,
    onToggleBookmarks: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    "Cari modus, tips, atau istilah...",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = SigapTheme.colors.textMuted,
                )
            },
            leadingIcon = {
                Icon(
                    Lucide.Search,
                    contentDescription = null,
                    tint = SigapTheme.colors.textMuted,
                    modifier = Modifier.size(18.dp),
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.weight(1f),
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isBookmarkedOnly) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (isBookmarkedOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onToggleBookmarks),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Lucide.Bookmark,
                    contentDescription = if (isBookmarkedOnly) "Tampilkan Semua" else "Artikel Tersimpan",
                    tint = if (isBookmarkedOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ThreatLandscapeCard() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header: LANSKAP ANCAMAN SIBER • Real-time badge
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "LANSKAP ANCAMAN SIBER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    ),
                    color = SigapTheme.colors.textMuted,
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Lucide.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "Real-time",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            // 3 Stat Columns
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Stat 1: 608 rb+
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "608 rb+",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Laporan penipuan",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }

                // Stat 2: Rp9,3 T
                Column(Modifier.weight(1.2f)) {
                    Text(
                        text = "Rp9,3 T",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                        color = Color(0xFFBA1A1A),
                    )
                    Text(
                        text = "Total kerugian",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }

                // Stat 3: 66%
                Column(Modifier.weight(0.9f)) {
                    Text(
                        text = "66%",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                        color = Color(0xFFD97706),
                    )
                    Text(
                        text = "Pernah dihubungi",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

            // Footer Source Note
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Sumber: IASC OJK, GASA. Data per Juni 2026.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
                Icon(
                    Lucide.Info,
                    contentDescription = null,
                    tint = SigapTheme.colors.textMuted,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun PopularArticleCard(
    categoryBadge: String,
    isTrending: Boolean,
    title: String,
    description: String,
    readTime: String,
    imageRes: Int? = null,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Column {
            if (imageRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                ) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Gradient scrim
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.6f),
                                    ),
                                ),
                            ),
                    )
                    // Badges inside image bottom
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        ) {
                            Text(
                                text = categoryBadge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }

                        if (isTrending) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD97706),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(
                                        Lucide.Flame,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp),
                                    )
                                    Text(
                                        text = "Trending",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (imageRes == null) {
                    // Fallback badges if no image
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Text(
                                text = categoryBadge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }

                        if (isTrending) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF3DC),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(
                                        Lucide.Flame,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        text = "Trending",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = Color(0xFFD97706),
                                    )
                                }
                            }
                        }
                    }
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 21.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                    ),
                    color = SigapTheme.colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Footer Read Time & Action Arrow
                Row(
                    Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            Lucide.Clock,
                            contentDescription = null,
                            tint = SigapTheme.colors.textMuted,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = readTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }

                    Box(
                        Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.ArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgilityQuizBanner(onPlay: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SigapTheme.colors.dominantSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onPlay),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Lucide.Zap,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = "Tes Ketangkasan",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "2 menit • Simulasi nyata",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = SigapTheme.colors.onDominantMuted,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Uji seberapa jeli Anda mendeteksi APK palsu",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 22.sp,
                    ),
                    color = SigapTheme.colors.onDominant,
                )
                Text(
                    text = "Latihan interaktif mengenali pesan jebakan sebelum terjadi.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = SigapTheme.colors.onDominantMuted,
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onPlay),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Mulai kuis sekarang",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Icon(
                        Lucide.ArrowRight,
                        contentDescription = "Mulai",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * S10B Artikel: Detail Artikel Edukasi Modus APK matching Mockup Image 1.
 */
@Composable
fun ArticleDetailScreen(
    articleId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val article = SeededTrends.ARTICLES.find { it.id == articleId } ?: SeededTrends.ARTICLES.first()

    val heroImageRes = when (article.id) {
        "apk_wedding_invitation" -> R.drawable.trend_apk_scam
        "fake_courier_photo" -> R.drawable.trend_apk_scam
        "online_shopping_marketplace" -> R.drawable.sample_phishing_proof
        "traffic_ticket_authority" -> R.drawable.sample_phishing_proof
        "part_time_job_commission" -> R.drawable.sample_phishing_proof
        else -> R.drawable.trend_apk_scam
    }

    val threatLabel = when (article.id) {
        "apk_wedding_invitation" -> "Tingkat Ancaman: Kritis (SMS Harvester)"
        "fake_courier_photo" -> "Tingkat Ancaman: Kritis (Pencuri SMS & OTP)"
        "online_shopping_marketplace" -> "Tingkat Ancaman: Tinggi (Transfer Fiktif)"
        "traffic_ticket_authority" -> "Tingkat Ancaman: Kritis (Malware Polantas)"
        "part_time_job_commission" -> "Tingkat Ancaman: Tinggi (Skema Ponzi Tugas)"
        else -> "Tingkat Ancaman: Waspada"
    }

    val versionBadge = when (article.id) {
        "apk_wedding_invitation" -> "v. Android 14 Tiruan"
        "fake_courier_photo" -> "Ekspedisi Tiruan (J&T/SiCepat)"
        "online_shopping_marketplace" -> "113.520 Laporan OJK"
        "traffic_ticket_authority" -> "Surat Tilang ETLE Palsu"
        "part_time_job_commission" -> "Modus Komisi Tugas Like"
        else -> "Modus Aktif 2025"
    }

    val categoryLabel = when (article.category) {
        ScamCategory.FAKE_INVITATION -> "Modus APK"
        ScamCategory.FAKE_COURIER -> "Kurir & Paket"
        ScamCategory.AUTHORITY_IMPERSONATION -> "Tilang Elektronik"
        ScamCategory.JOB_SCAM -> "Kerja Online"
        else -> "Penipuan Siber"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        // Top App Bar: Back arrow, "Detail Artikel", Share icon
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Lucide.ArrowLeft,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = "Detail Artikel",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            IconButton(
                onClick = {
                    shareArticle(context, article)
                },
            ) {
                Icon(
                    Lucide.Share2,
                    contentDescription = "Bagikan",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Article Content Body
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Tags: [● Category] [X mnt baca]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Text(
                            text = categoryLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Lucide.Clock,
                            contentDescription = null,
                            tint = SigapTheme.colors.textMuted,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "${article.readMinutes} mnt baca",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                }
            }

            // Article Title (Serif bold)
            Text(
                text = article.titleId,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )

            // Author & Date
            Text(
                text = "${article.sourceName}  •  ${article.sourceDate}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = SigapTheme.colors.textMuted,
            )

            // Hero Image with Overlays
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                Image(
                    painter = painterResource(heroImageRes),
                    contentDescription = article.titleId,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Dark gradient scrim for badge contrast
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.65f),
                                ),
                            ),
                        ),
                )

                // Overlay Pills at bottom of image
                Row(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(Modifier.size(6.dp).background(Color(0xFFBA1A1A), CircleShape))
                            Text(
                                text = threatLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                    ) {
                        Text(
                            text = versionBadge,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // Ringkasan Cepat Card (Soft Green container)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Lucide.Zap,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Ringkasan Cepat",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = article.summaryId,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // Section 1: Cara Kerjanya
            val stepCount = article.howItWorksId.size
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Cara Kerjanya",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Text(
                        text = "$stepCount Tahap Modus",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    article.howItWorksId.forEachIndexed { index, stepText ->
                        StepRow(
                            stepNumber = "${index + 1}",
                            title = "Tahap ${index + 1}",
                            description = stepText,
                        )
                        if (index < article.howItWorksId.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Section 2: Tanda Bahaya
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Lucide.TriangleAlert,
                    contentDescription = null,
                    tint = Color(0xFFBA1A1A),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Tanda Bahaya",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    article.redFlagsId.forEach { flagText ->
                        WarningBullet(text = flagText)
                    }
                }
            }

            // Section 3: Yang Harus Dilakukan
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Lucide.ShieldCheck,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Yang Harus Dilakukan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    article.actionsId.forEach { actionText ->
                        ActionCheckRow(text = actionText)
                    }
                }
            }

            // Source box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Lucide.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Sumber: ${article.sourceName} • ${article.sourceDate}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                }
            }

            // Bottom Action Button: Bagikan Artikel Ini
            Button(
                onClick = { shareArticle(context, article) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Lucide.Share2,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Bagikan Artikel Ini",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.Section))
        }
    }
}

@Composable
private fun StepRow(
    stepNumber: String,
    title: String,
    description: String,
    highlightText: String? = null,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (highlightText != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    ),
                    color = SigapTheme.colors.textMuted,
                )
            } else {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    ),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun WarningBullet(text: String, boldWords: List<String> = emptyList()) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .background(Color(0xFFFFECEB), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Lucide.TriangleAlert,
                contentDescription = null,
                tint = Color(0xFFBA1A1A),
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 17.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionCheckRow(text: String, boldWord: String? = null) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Lucide.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 17.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun shareArticle(context: Context, article: TrendArticle) {
    val shareBody = buildString {
        appendLine("Waspada: Undangan nikah digital berformat APK!")
        appendLine()
        appendLine("Pelaku mengirimkan berkas berekstensi .apk dengan nama pura-pura undangan pernikahan untuk mencuri SMS OTP perbankan.")
        appendLine()
        appendLine("Tips Lindungi Diri:")
        appendLine("1. Jangan pernah instal berkas .apk dari chat WhatsApp/SMS")
        appendLine("2. Undangan asli selalu berupa tautan web, bukan aplikasi")
        appendLine("3. Gunakan aplikasi Sigap untuk proteksi otomatis")
        appendLine()
        append("Sumber: Direktorat Tindak Pidana Siber Bareskrim Polri & OJK Satgas PASTI")
    }
    val intent = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_SUBJECT, "Edukasi Modus Penipuan APK - Sigap")
        .putExtra(Intent.EXTRA_TEXT, shareBody)
    context.startActivity(Intent.createChooser(intent, "Bagikan Informasi Modus"))
}
