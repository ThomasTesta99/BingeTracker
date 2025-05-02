package com.example.bingetracker.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bingetracker.data.HomeContentFilter
import com.example.bingetracker.data.HomeContentSort

@Composable
fun HomeFilterBar(
    currentFilter: HomeContentFilter,
    currentSort: HomeContentSort,
    onFilterChanged: (HomeContentFilter) -> Unit,
    onSortChanged: (HomeContentSort) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter buttons in a row
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallToggleButton(
                    text = "All",
                    selected = currentFilter == HomeContentFilter.ALL,
                    onClick = { onFilterChanged(HomeContentFilter.ALL) }
                )

                Spacer(modifier = Modifier.width(4.dp))

                SmallToggleButton(
                    text = "Movies",
                    selected = currentFilter == HomeContentFilter.MOVIES,
                    onClick = { onFilterChanged(HomeContentFilter.MOVIES) }
                )

                Spacer(modifier = Modifier.width(4.dp))

                SmallToggleButton(
                    text = "TV Shows",
                    selected = currentFilter == HomeContentFilter.TV_SHOWS,
                    onClick = { onFilterChanged(HomeContentFilter.TV_SHOWS) }
                )
            }

            // Sort dropdown on the right side
            Column(
                //verticalAlignment = Alignment.CenterVertically
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sort by:",
                    style = MaterialTheme.typography.bodySmall
                )

                var expanded by remember { mutableStateOf(false) }

                TextButton(
                    onClick = { expanded = true },
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        getSortName(currentSort),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Popular") },
                        onClick = {
                            onSortChanged(HomeContentSort.POPULARITY)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Newest") },
                        onClick = {
                            onSortChanged(HomeContentSort.NEWEST)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rating") },
                        onClick = {
                            onSortChanged(HomeContentSort.RATING)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.padding(0.dp),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = text,
            color = if (selected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun getSortName(sort: HomeContentSort): String {
    return when (sort) {
        HomeContentSort.POPULARITY -> "Popular"
        HomeContentSort.NEWEST -> "Newest"
        HomeContentSort.RATING -> "Rating"
    }
}