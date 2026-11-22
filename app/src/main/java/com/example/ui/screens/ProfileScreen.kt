package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DiajakCatalog
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.ui.DiajakViewModel
import com.example.ui.auth.signOutUser
import com.example.ui.components.DiajakFlatSecondaryButton
import com.example.ui.components.DiajakFlatTextField
import com.example.ui.components.InitialAvatar
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.VerifiedBlue
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenSurface
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberSurface
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: DiajakViewModel,
    onOpenOrganizerPortal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val bookingsState by viewModel.participantBookingsState.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIdsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val profile = (profileState as? UiState.Success)?.data ?: UserProfile(
        userId = viewModel.currentUserId.orEmpty(),
        displayName = Firebase.auth.currentUser?.displayName ?: "Pengguna Diajak",
        photoUrl = Firebase.auth.currentUser?.photoUrl?.toString().orEmpty()
    )
    val userEmail = Firebase.auth.currentUser?.email.orEmpty()
    val bookingCount = (bookingsState as? UiState.Success)?.data?.size ?: 0

    var showEditDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = DiajakSpacing.ScreenHorizontal, vertical = DiajakSpacing.SectionGap),
        verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
    ) {
        item {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Clean Instagram-style profile summary card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DiajakRadius.Card)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialAvatar(
                        name = profile.displayName,
                        photoKey = profile.photoUrl,
                        size = 68.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = profile.displayName.ifBlank { "Pengguna Diajak" },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (profile.organizerVerified) {
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = "Terverifikasi",
                                    tint = VerifiedBlue,
                                    modifier = Modifier.size(DiajakIconSize.Inline)
                                )
                            }
                        }
                        if (userEmail.isNotBlank()) {
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = profile.city,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (profile.bio.isNotBlank()) {
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (profile.interests.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        profile.interests.forEach { tag ->
                            Surface(
                                shape = DiajakRadius.Pill,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatBlock(value = "$bookingCount", label = "Tiket")
                    ProfileStatBlock(value = "${favoriteIds.size}", label = "Favorit")
                    ProfileStatBlock(
                        value = if (profile.organizerVerified) "Aktif" else "Belum",
                        label = "Status KTP"
                    )
                }
            }
        }

        // Shopee-style Organizer Portal Entrance Card
        item {
            Surface(
                onClick = onOpenOrganizerPortal,
                shape = DiajakRadius.Card,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_organizer_portal_card")
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(DiajakComponentSize.ActionCircle)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(DiajakIconSize.Primary)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Halaman Penyelenggara",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Kelola aktivitas, pendaftaran, pesan, saldo, ulasan & dokumen KTP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = DiajakRadius.Pill,
                            color = if (profile.organizerVerified) VerifiedGreenSurface else WarningAmberSurface
                        ) {
                            Text(
                                text = if (profile.organizerVerified) "Terverifikasi KTP" else "Lengkapi KTP untuk Terbitkan Event",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (profile.organizerVerified) VerifiedGreen else WarningAmber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Buka Portal Penyelenggara",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(DiajakIconSize.Primary)
                    )
                }
            }
        }

        // Clean Neutral Gray Menu Container
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DiajakRadius.Card)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                ProfileMenuItem(
                    icon = Icons.Rounded.Edit,
                    title = "Edit Profil",
                    subtitle = "Ubah nama, bio, kota, dan foto profil",
                    onClick = { showEditDialog = true },
                    testTag = "profile_edit_button"
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                ProfileMenuItem(
                    icon = Icons.Rounded.Info,
                    title = "Tentang Aplikasi",
                    subtitle = "Mengenal diajak (diajak.com) & misi komunitas",
                    onClick = { showAboutDialog = true },
                    testTag = "profile_about_button"
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                ProfileMenuItem(
                    icon = Icons.Rounded.Policy,
                    title = "Kebijakan Privasi",
                    subtitle = "Perlindungan data pribadi, KTP & lokasi",
                    onClick = { showPrivacyDialog = true },
                    testTag = "profile_privacy_button"
                )
            }
        }

        // Sign Out Button
        item {
            DiajakFlatSecondaryButton(
                onClick = {
                    signOutUser(credentialManager = credentialManager, scope = scope)
                },
                contentColor = ErrorRed,
                shape = DiajakRadius.Control,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DiajakComponentSize.PrimaryButtonHeight)
                    .testTag("profile_sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(DiajakIconSize.Inline)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Keluar Akun",
                    style = MaterialTheme.typography.labelLarge,
                    color = ErrorRed
                )
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { showEditDialog = false },
            onSave = { name, bio, city, photo ->
                viewModel.updateProfile(
                    displayName = name,
                    bio = bio,
                    city = city,
                    photoUrl = photo,
                    interests = profile.interests
                ) {
                    showEditDialog = false
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            title = { Text("Tentang diajak (diajak.com)", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "diajak adalah social activity marketplace yang mempertemukan peserta, penyelenggara, dan komunitas di seluruh Indonesia.\n\nTemukan aktivitas gratis maupun berbayar mulai dari Hyrox, lari, padel, e-sport, fotografi, kopi, hingga 69 kategori hobi lainnya. Setiap penyelenggara diverifikasi melalui dokumen KTP untuk menjaga keamanan dan kualitas acara komunitas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    shape = DiajakRadius.Control,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
                ) { Text("Tutup", style = MaterialTheme.typography.labelLarge) }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            title = { Text("Kebijakan Privasi", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "1. Autentikasi Aman: Akun Anda dilindungi Google Sign-In melalui Jetpack Credential Manager.\n\n2. Isolasi Data KTP: Dokumen KTP yang diunggah di Portal Penyelenggara disimpan secara privat hanya untuk akun pemilik dan tidak dapat dilihat pengguna lain.\n\n3. Lokasi Opsional: Izin lokasi kasar (GPS) hanya digunakan untuk menghitung jarak aktivitas terdekat, dan Anda dapat memilih kota secara manual kapan saja.\n\n4. Pemilihan Foto Tanpa Izin Penyimpanan Luas: Aplikasi menggunakan Android Photo Picker bawaan sistem.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    shape = DiajakRadius.Control,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
                ) { Text("Mengerti", style = MaterialTheme.typography.labelLarge) }
            }
        )
    }
}

@Composable
private fun ProfileStatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(DiajakComponentSize.IconCircle)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(DiajakIconSize.Primary)
                )
            }
            Spacer(modifier = Modifier.width(DiajakSpacing.ItemGap))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(DiajakIconSize.Primary)
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(profile.displayName) }
    var bio by remember { mutableStateOf(profile.bio) }
    var city by remember { mutableStateOf(profile.city.ifBlank { "Jakarta Selatan" }) }
    var photoUri by remember { mutableStateOf(profile.photoUrl) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) photoUri = uri.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = { Text("Edit Profil", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InitialAvatar(name = name, photoKey = photoUri, size = 54.dp, nested = false)
                    DiajakFlatSecondaryButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = DiajakRadius.Small
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(DiajakIconSize.Inline)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih Foto", style = MaterialTheme.typography.labelMedium)
                    }
                }
                DiajakFlatTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nama Tampilan",
                    singleLine = true,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
                DiajakFlatTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio Singkat",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
                DiajakFlatTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "Kota Domisili",
                    placeholder = DiajakCatalog.cities.first().name,
                    singleLine = true,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, bio, city, photoUri) },
                enabled = name.trim().isNotEmpty(),
                shape = DiajakRadius.Control,
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Text("Simpan", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
