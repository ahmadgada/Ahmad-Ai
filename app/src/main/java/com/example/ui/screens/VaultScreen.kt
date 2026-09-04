package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import com.example.ui.animation.pressScale
import com.example.ui.animation.tactileClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppNavDestination
import com.example.model.VaultCategory
import com.example.model.VaultItem
import com.example.model.VaultType
import com.example.ui.components.VaultItemCard
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
import com.example.ui.theme.FrostedGlassSurface
import com.example.ui.theme.FrostedGlassSurfaceHighlight
import com.example.ui.theme.FrostedGlassSurfaceVariant
import com.example.ui.theme.NebulaBlue
import com.example.ui.theme.NebulaFuchsia
import com.example.ui.theme.NebulaIndigo
import com.example.ui.theme.TextIndigoSubtle
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary

@Composable
fun VaultScreen(
    items: List<VaultItem>,
    searchQuery: String,
    selectedCategory: VaultCategory,
    selectedTypeFilter: VaultType?,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (VaultCategory) -> Unit,
    onTypeFilterSelect: (VaultType?) -> Unit,
    onItemClick: (VaultItem) -> Unit,
    onFavoriteToggle: (VaultItem) -> Unit,
    onCopyPassword: (VaultItem) -> Unit,
    onCopyUsername: (VaultItem) -> Unit,
    onEditItem: (VaultItem) -> Unit,
    onDeleteItem: (VaultItem) -> Unit,
    onAddNewItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SECURE VAULT",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextWhitePrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${items.size} credential${if (items.size == 1) "" else "s"} fortified",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextIndigoSubtle
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NebulaIndigo, NebulaFuchsia))
                        )
                        .border(1.dp, FrostedGlassBorderLight, CircleShape)
                        .tactileClickable(targetScale = 0.90f, onClick = onAddNewItem)
                        .testTag("vault_add_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Item",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search vault entries, websites, tags...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NebulaIndigo
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextMutedSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NebulaIndigo,
                    unfocusedBorderColor = FrostedGlassBorderLight,
                    focusedContainerColor = FrostedGlassSurfaceVariant,
                    unfocusedContainerColor = FrostedGlassSurfaceVariant,
                    focusedTextColor = TextWhitePrimary,
                    unfocusedTextColor = TextWhitePrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_search_input")
            )
        }

        // Type Filter Tabs
        val typesList = listOf<VaultType?>(null) + VaultType.entries
        ScrollableTabRow(
            selectedTabIndex = typesList.indexOf(selectedTypeFilter),
            edgePadding = 18.dp,
            containerColor = Color.Transparent,
            contentColor = NebulaIndigo,
            indicator = { tabPositions ->
                val index = typesList.indexOf(selectedTypeFilter)
                if (index in tabPositions.indices) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = NebulaIndigo,
                        height = 3.dp
                    )
                }
            },
            divider = {}
        ) {
            typesList.forEach { type ->
                val selected = selectedTypeFilter == type
                Tab(
                    selected = selected,
                    onClick = { onTypeFilterSelect(type) },
                    text = {
                        Text(
                            text = type?.displayName ?: "All Credentials",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selected) NebulaIndigo else TextMutedSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Categories Chips Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(VaultCategory.entries) { category ->
                val selected = selectedCategory == category
                val chipBg by animateColorAsState(
                    targetValue = if (selected) NebulaIndigo.copy(alpha = 0.25f) else FrostedGlassSurfaceVariant,
                    animationSpec = tween(180),
                    label = "chip_bg"
                )
                val chipBorder by animateColorAsState(
                    targetValue = if (selected) NebulaIndigo.copy(alpha = 0.7f) else FrostedGlassBorder,
                    animationSpec = tween(180),
                    label = "chip_border"
                )
                val chipText by animateColorAsState(
                    targetValue = if (selected) TextWhitePrimary else TextMutedSecondary,
                    animationSpec = tween(180),
                    label = "chip_text"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(chipBg)
                        .border(1.dp, chipBorder, RoundedCornerShape(16.dp))
                        .pressScale(0.95f)
                        .clickable { onCategorySelect(category) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("category_${category.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = chipText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Items List or Empty State
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(FrostedGlassSurface)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(28.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(NebulaIndigo.copy(alpha = 0.18f))
                                .border(1.dp, NebulaIndigo.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Lock,
                                contentDescription = null,
                                tint = NebulaIndigo,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) "NO RESULTS FOUND" else "VAULT IS EMPTY",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhitePrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "No credentials match '$searchQuery'. Try checking another category or search term."
                            else
                                "Add passwords, bank cards, identities, or notes to secure them.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMutedSecondary,
                            textAlign = TextAlign.Center
                        )

                        if (searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onAddNewItem,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NebulaIndigo,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Secure Item", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    VaultItemCard(
                        item = item,
                        onItemClick = { onItemClick(item) },
                        onFavoriteToggle = { onFavoriteToggle(item) },
                        onCopyPassword = { onCopyPassword(item) },
                        onCopyUsername = { onCopyUsername(item) },
                        onEditClick = { onEditItem(item) },
                        onDeleteClick = { onDeleteItem(item) }
                    )
                }
            }
        }
    }
}
