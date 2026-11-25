package com.example.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.DevBypassSession
import com.example.ui.components.DiajakFlatSecondaryButton
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    reasonMessage: String? = null,
    onDismiss: (() -> Unit)? = null,
    onDevBypassLogin: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var isCheckingAuto by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (onDismiss != null) {
        BackHandler(onBack = onDismiss)
    }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isCheckingAuto = false
                onAuthSuccess()
            },
            onUnauthenticated = {
                isCheckingAuto = false
            },
            scope = scope
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        val availableHeight = maxHeight
        val isTallViewport = availableHeight >= 780.dp
        val isCompactViewport = availableHeight < 680.dp
        val collageHeight = when {
            isTallViewport -> 236.dp
            isCompactViewport -> 168.dp
            else -> 200.dp
        }
        val sectionGap = when {
            isTallViewport -> 20.dp
            isCompactViewport -> 12.dp
            else -> 16.dp
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .heightIn(min = availableHeight)
                .padding(horizontal = DiajakSpacing.ScreenHorizontal, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!reasonMessage.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DiajakRadius.Control)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LockOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(DiajakIconSize.Inline)
                    )
                    Text(
                        text = reasonMessage,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = sectionGap),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Editorial photo collage showcasing real activities
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(collageHeight),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_activity_running),
                        contentDescription = "Komunitas lari diajak",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1.25f)
                            .fillMaxSize()
                            .clip(DiajakRadius.Hero)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_activity_padel),
                            contentDescription = "Aktivitas padel diajak",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(DiajakRadius.Card)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.img_activity_photowalk),
                            contentDescription = "Fotografi dan kopi diajak",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(DiajakRadius.Card)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(sectionGap))

                Text(
                    text = "Discover, connect, create, experience.",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Diajak membantu menemukan aktivitas, menghubungkan melalui minat, kesempatan membuat aktivitas, dan wujudkan pengalaman nyata.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = DiajakRadius.Control,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (DevBypassSession.ENABLE_DEV_LOGIN_BYPASS && onDevBypassLogin != null) {
                    Button(
                        onClick = {
                            errorMessage = null
                            onDevBypassLogin()
                        },
                        enabled = !isLoading,
                        shape = DiajakRadius.Control,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DiajakComponentSize.PrimaryButtonHeight)
                            .testTag("dev_bypass_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(DiajakIconSize.Inline)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bypass Login (Dev Sementara)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    enabled = !isLoading && !isCheckingAuto,
                    shape = DiajakRadius.Control,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DiajakComponentSize.PrimaryButtonHeight)
                        .testTag("google_sign_in_button")
                ) {
                    if (isLoading || isCheckingAuto) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_g),
                            contentDescription = null,
                            modifier = Modifier.size(DiajakIconSize.Primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Masuk dengan Google",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (onDismiss != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DiajakFlatSecondaryButton(
                        onClick = onDismiss,
                        shape = DiajakRadius.Control,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DiajakComponentSize.PrimaryButtonHeight)
                            .testTag("auth_continue_guest_button")
                    ) {
                        Text(
                            text = "Nanti saja, lanjut jelajah",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dengan melanjutkan, Anda menyetujui Kebijakan Privasi & Ketentuan Komunitas diajak.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
