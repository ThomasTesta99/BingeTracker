package com.example.bingetracker.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter chips
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    text = "All",
                    selected = currentFilter == BingeFilter.ALL,
                    onClick = { onFilterChanged(BingeFilter.ALL) }
                )

                Spacer(modifier = Modifier.width(6.dp))

                FilterChip(
                    text = "Movies",
                    selected = currentFilter == BingeFilter.MOVIES_ONLY,
                    onClick = { onFilterChanged(BingeFilter.MOVIES_ONLY) }
                )

                Spacer(modifier = Modifier.width(6.dp))

                FilterChip(
                    text = "TV",
                    selected = currentFilter == BingeFilter.TV_SHOWS_ONLY,
                    onClick = { onFilterChanged(BingeFilter.TV_SHOWS_ONLY) }
                )
            }

            // Sort dropdown
            SortDropdown(
                currentSort = currentSort,
                onSortChanged = onSortChanged
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .height(32.dp)
            .padding(0.dp),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (selected)
                Color(0xFFAA00FF).copy(alpha = 0.1f)
            else
                Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            color = if (selected)
                Color(0xFFAA00FF)
            else
                Color.Gray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SortDropdown(
    currentSort: BingeSort,
    onSortChanged: (BingeSort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                getSortName(currentSort),
                fontSize = 12.sp,
                color = Color(0xFFAA00FF),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Sort options",
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation),
                tint = Color(0xFFAA00FF)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("A-Z", fontSize = 12.sp) },
                onClick = {
                    onSortChanged(BingeSort.ALPHABETICAL)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Progress", fontSize = 12.sp) },
                onClick = {
                    onSortChanged(BingeSort.PROGRESS)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Recently Updated", fontSize = 12.sp) },
                onClick = {
                    onSortChanged(BingeSort.RECENTLY_UPDATED)
                    expanded = false
                }
            )
        }
    }
}

private fun getSortName(sort: BingeSort): String {
    return when (sort) {
        BingeSort.ALPHABETICAL -> "A-Z"
        BingeSort.PROGRESS -> "Progress"
        BingeSort.RECENTLY_UPDATED -> "Recently"
    }
}