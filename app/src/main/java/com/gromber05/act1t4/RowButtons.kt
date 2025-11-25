package com.gromber05.act1t4

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RowButtons(
    onPlay: () -> Unit,
    onPause: () -> Unit,
    playText: String,
    pauseText: String
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = onPlay,
            modifier = Modifier
                .weight(1f)
        ) {
            Text(text = playText)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onPause,
            modifier = Modifier
                .weight(1f)
        ) {
            Text(text = pauseText)
        }
    }
}
