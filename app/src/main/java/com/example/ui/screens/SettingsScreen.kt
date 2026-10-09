package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AnimeRecsViewModel
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimePink
import com.example.ui.theme.AnimeViolet
import com.example.ui.theme.AnimeVioletLight
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenMatch
import com.example.ui.theme.RedAdult
import com.example.ui.theme.RedAdultBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.data.model.AniListAccount

@Composable
fun SettingsScreen(
    viewModel: AnimeRecsViewModel,
    modifier: Modifier = Modifier
) {
    val isAdultEnabled by viewModel.preferences.isAdultContentEnabled.collectAsState()
    val hasConfirmedAge18 by viewModel.preferences.hasConfirmedAge18.collectAsState()
    val savedIds by viewModel.preferences.savedMediaIds.collectAsState()
    val viewer by viewModel.viewer.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val activeViewerId by viewModel.activeViewerId.collectAsState()

    var accountToRemove by remember { mutableStateOf<AniListAccount?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Settings & Preferences",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Control multi-account sync, 18+ maturity rules, and recommendation preferences",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // Section 1: AniList Multi-Account Connection
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (accounts.isNotEmpty()) AnimeCyan.copy(alpha = 0.5f) else AnimeViolet.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = AnimeViolet.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = AnimeCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AniList Accounts (${accounts.size})",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (accounts.isNotEmpty()) "Active: @${viewer?.name ?: "Selected"}" else "Multi-account OAuth Implicit Grant",
                                    color = if (accounts.isNotEmpty()) AnimeCyan else TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (accounts.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GreenMatch.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("${accounts.size} CONNECTED", color = GreenMatch, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = DarkBorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (accounts.isNotEmpty()) {
                        Text(
                            text = "Connected accounts. Switch active profile anytime to update personalized recommendations and sync your library.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // List of accounts
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            accounts.forEach { account ->
                                val isActive = account.viewerId == (activeViewerId ?: viewer?.id)
                                Surface(
                                    color = if (isActive) AnimeViolet.copy(alpha = 0.15f) else DarkSurfaceElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isActive) AnimeCyan.copy(alpha = 0.6f) else DarkBorderSubtle
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!isActive) viewModel.switchAccount(account.viewerId)
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (!account.avatarUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = account.avatarUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .border(
                                                            1.dp,
                                                            if (isActive) AnimeCyan else Color.Transparent,
                                                            CircleShape
                                                        )
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isActive) AnimeViolet else DarkBorder),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = account.viewerName.take(1).uppercase(),
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "@${account.viewerName}",
                                                        color = if (isActive) Color.White else TextPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (isActive) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .background(GreenMatch.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = "ACTIVE",
                                                                color = GreenMatch,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "ID: ${account.viewerId}",
                                                    color = TextMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!isActive) {
                                                TextButton(
                                                    onClick = { viewModel.switchAccount(account.viewerId) }
                                                ) {
                                                    Text("Switch", color = AnimeCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            IconButton(
                                                onClick = { accountToRemove = account },
                                                modifier = Modifier.size(32.dp).testTag("settings_remove_account_${account.viewerId}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Remove Account",
                                                    tint = Color(0xFFE57373),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openAniListAuth() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AnimeCyan
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AnimeCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f).testTag("settings_add_another_account_button")
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            androidx.compose.material3.Button(
                                onClick = { viewModel.disconnectAllAccounts() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_disconnect_all_button")
                            ) {
                                Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "Connect your AniList profile to sync your watching & reading lists, and receive personalized recommendations powered by your 8+ rated favorites.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.openAniListAuth() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AnimeViolet,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("settings_connect_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect AniList Account", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 2: Content & Maturity (18+)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdultEnabled) RedAdult.copy(alpha = 0.5f) else DarkBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isAdultEnabled) RedAdultBg else DarkSurfaceElevated,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isAdultEnabled) Icons.Default.Warning else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isAdultEnabled) RedAdult else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Show Adult Content (18+)",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isAdultEnabled) "Enabled (Blur on by default)" else "Disabled (Filtered out)",
                                    color = if (isAdultEnabled) RedAdult else TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = isAdultEnabled,
                            onCheckedChange = { checked ->
                                viewModel.requestToggleAdultContent(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RedAdult,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkSurfaceElevated
                            ),
                            modifier = Modifier.testTag("adult_content_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = DarkBorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Rules explanation
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasConfirmedAge18) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (hasConfirmedAge18) GreenMatch else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasConfirmedAge18) "Age verified: Confirmed 18+" else "One-time 18+ attestation required to enable",
                                color = if (hasConfirmedAge18) GreenMatch else TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "• When OFF: All 18+ titles are strictly excluded at query level (isAdult: false).",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "• When ON: Covers are protected by a tap-to-reveal blur overlay and tagged with a red 18+ badge.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "• Discovery page excludes 18+ titles unless adult toggle is explicitly enabled.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section 2: Recommendation Formula Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, AnimeViolet.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AnimeVioletLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recommendation Formula",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "AniList recommendation community signals are mathematically blended with dynamic metadata similarity:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "finalScore = (genreOverlap * 0.35)\n           + (tagOverlap * 0.30)\n           + (aniListRecScore * 0.20)\n           + (formatMatch * 0.15)",
                                color = AnimeCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FormulaTierChip(label = "90-100%", desc = "Green (High)", color = GreenMatch)
                        FormulaTierChip(label = "70-89%", desc = "Yellow (Mid)", color = com.example.ui.theme.YellowMatch)
                        FormulaTierChip(label = "< 70%", desc = "Gray (Low)", color = com.example.ui.theme.GrayMatch)
                    }
                }
            }
        }

        // Section 3: Data Source & API
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = AnimeCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AniList GraphQL Engine",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Powered by AniList's public GraphQL API (https://graphql.anilist.co). Features instant debounced search, Manhwa KR origin detection, and live anime & manga data without requiring private user API keys.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Bookmarks saved locally: ${savedIds.size}",
                            color = AnimePink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "v1.0.0",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Account Removal Confirmation Dialog
    if (accountToRemove != null) {
        val acc = accountToRemove!!
        AlertDialog(
            onDismissRequest = { accountToRemove = null },
            containerColor = DarkSurfaceCard,
            title = {
                Text(
                    text = "Remove @${acc.viewerName}?",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will remove @${acc.viewerName}'s AniList token from this device. You can reconnect it anytime.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        val idToRemove = acc.viewerId
                        accountToRemove = null
                        viewModel.removeAccount(idToRemove)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToRemove = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun FormulaTierChip(label: String, desc: String, color: Color) {
    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(text = label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = TextMuted, fontSize = 10.sp)
        }
    }
}
