package com.example.bingetracker.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Content type filter buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ToggleButton(
                    text = "All",
                    selected = currentFilter == HomeContentFilter.ALL,
                    onClick = { onFilterChanged(HomeContentFilter.ALL) }
                )

                Spacer(modifier = Modifier.width(8.dp))

                ToggleButton(
                    text = "Movies",
                    selected = currentFilter == HomeContentFilter.MOVIES,
                    onClick = { onFilterChanged(HomeContentFilter.MOVIES) }
                )

                Spacer(modifier = Modifier.width(8.dp))

                ToggleButton(
                    text = "TV Shows",
                    selected = currentFilter == HomeContentFilter.TV_SHOWS,
                    onClick = { onFilterChanged(HomeContentFilter.TV_SHOWS) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sort options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort by:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(8.dp))

                var expanded by remember { mutableStateOf(false) }

                TextButton(onClick = { expanded = true }) {
                    Text(getSortName(currentSort))
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
private fun ToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
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
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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