package com.example.bingetracker.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bingetracker.data.BingeFilter
import com.example.bingetracker.data.BingeSort

@Composable
fun FilterSortBar(
    currentFilter: BingeFilter,
    currentSort: BingeSort,
    onFilterChanged: (BingeFilter) -> Unit,
    onSortChanged: (BingeSort) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter dropdown
            var expandedFilter by remember { mutableStateOf(false) }
            Column {
                Row(
                    modifier = Modifier.clickable { expandedFilter = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter: ${getFilterName(currentFilter)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Filter options")
                }

                DropdownMenu(
                    expanded = expandedFilter,
                    onDismissRequest = { expandedFilter = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            onFilterChanged(BingeFilter.ALL)
                            expandedFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Movies Only") },
                        onClick = {
                            onFilterChanged(BingeFilter.MOVIES_ONLY)
                            expandedFilter = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("TV Shows Only") },
                        onClick = {
                            onFilterChanged(BingeFilter.TV_SHOWS_ONLY)
                            expandedFilter = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Sort dropdown
            var expandedSort by remember { mutableStateOf(false) }
            Column {
                Row(
                    modifier = Modifier.clickable { expandedSort = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by: ${getSortName(currentSort)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(Icons.Default.Sort, contentDescription = "Sort options")
                }

                DropdownMenu(
                    expanded = expandedSort,
                    onDismissRequest = { expandedSort = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("A-Z") },
                        onClick = {
                            onSortChanged(BingeSort.ALPHABETICAL)
                            expandedSort = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Progress") },
                        onClick = {
                            onSortChanged(BingeSort.PROGRESS)
                            expandedSort = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Recently Updated") },
                        onClick = {
                            onSortChanged(BingeSort.RECENTLY_UPDATED)
                            expandedSort = false
                        }
                    )
                }
            }
        }
    }
}

private fun getFilterName(filter: BingeFilter): String {
    return when (filter) {
        BingeFilter.ALL -> "All"
        BingeFilter.MOVIES_ONLY -> "Movies Only"
        BingeFilter.TV_SHOWS_ONLY -> "TV Shows Only"
    }
}

private fun getSortName(sort: BingeSort): String {
    return when (sort) {
        BingeSort.ALPHABETICAL -> "A-Z"
        BingeSort.PROGRESS -> "Progress"
        BingeSort.RECENTLY_UPDATED -> "Recently Updated"
    }
}