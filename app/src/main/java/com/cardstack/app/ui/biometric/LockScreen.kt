package com.cardstack.app.ui.biometric

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.cardstack.app.R
import com.cardstack.app.ui.theme.IndigoAccent

@Composable
fun LockScreen(onRetry: () -> Unit, errorMessage: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Brand logo (icon + wordmark)
            Image(
                painter = painterResource(R.drawable.logo_wordmark),
                contentDescription = "CardStack",
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .aspectRatio(2.47f),      // original 862:349 ratio
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(48.dp))

            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Authenticate",
                tint = IndigoAccent,
                modifier = Modifier.size(64.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
                ) {
                    Text("Try Again")
                }
            } else {
                Text(
                    "Touch the sensor to unlock",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9EBD)
                )
            }
        }
    }
}
