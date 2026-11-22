package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.ActivityItem
import com.example.data.DiajakRepository
import com.example.data.LocationService
import com.example.data.UiState
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.ActivityDetailScreen
import com.example.ui.screens.BookingCheckoutScreen
import com.example.ui.screens.ChatRoomScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ManageActivitiesScreen
import com.example.ui.screens.ManageBalanceScreen
import com.example.ui.screens.ManageDocumentsScreen
import com.example.ui.screens.ManageOrganizerMessagesScreen
import com.example.ui.screens.ManageRegistrationsScreen
import com.example.ui.screens.ManageReviewsScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.OrganizerModuleRoute
import com.example.ui.screens.OrganizerPortalScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TicketsScreen
import com.example.ui.theme.DiajakIconSize
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val ROUTE_HOME = "home"
private const val ROUTE_FAVORITES = "favorites"
private const val ROUTE_TICKETS = "tickets"
private const val ROUTE_MESSAGES = "messages"
private const val ROUTE_PROFILE = "profile"
private const val ROUTE_DETAIL = "detail/{activityId}"
private const val ROUTE_BOOKING = "booking/{activityId}"
private const val ROUTE_CHAT_ROOM = "chat/{conversationId}"
private const val ROUTE_ORGANIZER_PORTAL = "organizer_portal"
private const val ROUTE_LOGIN = "login"

private sealed interface PendingAuthAction {
    data class OpenTab(val route: String, val messagesTab: Int = 0) : PendingAuthAction
    data class OpenBooking(val activityId: String) : PendingAuthAction
    data class ToggleFavorite(val activity: ActivityItem) : PendingAuthAction
    data class OpenOrganizerChat(val activity: ActivityItem) : PendingAuthAction
    data object StayOnCurrentScreen : PendingAuthAction
}

private data class BottomTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomTabItem(ROUTE_HOME, "Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    BottomTabItem(ROUTE_FAVORITES, "Favorit", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    BottomTabItem(ROUTE_TICKETS, "Tiket", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
    BottomTabItem(ROUTE_MESSAGES, "Pesan", Icons.Rounded.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    BottomTabItem(ROUTE_PROFILE, "Profil", Icons.Filled.Person, Icons.Outlined.PersonOutline)
)

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun DiajakAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    DiajakMainShell(currentUserId = currentUser?.uid)
}

@Composable
private fun DiajakMainShell(currentUserId: String?) {
    val isSignedIn = !currentUserId.isNullOrBlank()
    val viewModel: DiajakViewModel = viewModel(
        key = currentUserId ?: "guest",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) { "APPLICATION_KEY missing" }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                DiajakViewModel(
                    repository = DiajakRepository(db),
                    locationService = LocationService(app),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val unreadNotifCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val publishedState by viewModel.publishedActivitiesState.collectAsStateWithLifecycle()
    val organizerActivitiesState by viewModel.organizerActivitiesState.collectAsStateWithLifecycle()
    val conversationsState by viewModel.conversationsState.collectAsStateWithLifecycle()

    var messagesInitialTab by remember { mutableIntStateOf(0) }
    var pendingAuthAction by remember { mutableStateOf<PendingAuthAction?>(null) }
    var authPromptReason by remember { mutableStateOf<String?>(null) }

    val navigateToTopTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val requestAuth: (String, PendingAuthAction) -> Unit = { reason, action ->
        authPromptReason = reason
        pendingAuthAction = action
        navController.navigate(ROUTE_LOGIN) {
            launchSingleTop = true
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.toastEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Handle post-login continuation & clean return to Beranda on sign-out
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            if (navController.currentDestination?.route == ROUTE_LOGIN) {
                navController.popBackStack()
            }
            val pending = pendingAuthAction
            pendingAuthAction = null
            authPromptReason = null
            when (pending) {
                is PendingAuthAction.OpenTab -> {
                    if (pending.route == ROUTE_MESSAGES) messagesInitialTab = pending.messagesTab
                    navigateToTopTab(pending.route)
                }
                is PendingAuthAction.OpenBooking -> {
                    navController.navigate("booking/${pending.activityId}")
                }
                is PendingAuthAction.ToggleFavorite -> {
                    viewModel.toggleFavorite(pending.activity)
                }
                is PendingAuthAction.OpenOrganizerChat -> {
                    viewModel.openDirectChatWithOrganizer(pending.activity) { convId ->
                        navController.navigate("chat/$convId")
                    }
                }
                PendingAuthAction.StayOnCurrentScreen, null -> Unit
            }
        } else {
            val activeRoute = navController.currentDestination?.route
            if (activeRoute != null && activeRoute != ROUTE_HOME && activeRoute != ROUTE_DETAIL && activeRoute != ROUTE_LOGIN) {
                navController.navigate(ROUTE_HOME) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    val topLevelRoutes = remember { bottomTabs.map { it.route }.toSet() }
    val showBottomBar = currentRoute in topLevelRoutes

    val handleTabClick: (BottomTabItem) -> Unit = { tab ->
        if (tab.route == ROUTE_HOME || isSignedIn) {
            if (tab.route == ROUTE_MESSAGES) messagesInitialTab = 0
            navigateToTopTab(tab.route)
        } else {
            val reason = when (tab.route) {
                ROUTE_FAVORITES -> "Masuk dengan Google untuk menyimpan & melihat daftar Favoritmu."
                ROUTE_TICKETS -> "Masuk dengan Google untuk melihat bukti booking & E-Tiket kamu."
                ROUTE_MESSAGES -> "Masuk dengan Google untuk membuka Chat Penyelenggara, Grup Aktivitas & Notifikasi."
                ROUTE_PROFILE -> "Masuk dengan Google untuk mengakses Profil & Portal Penyelenggara."
                else -> "Masuk dengan Google untuk melanjutkan."
            }
            requestAuth(reason, PendingAuthAction.OpenTab(tab.route))
        }
    }

    val handleToggleFavorite: (ActivityItem) -> Unit = { activity ->
        if (isSignedIn) {
            viewModel.toggleFavorite(activity)
        } else {
            requestAuth(
                "Masuk dengan Google untuk menyimpan aktivitas ke Favorit.",
                PendingAuthAction.ToggleFavorite(activity)
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        // NON-FLOATING Material Design docked bottom navigation bar (Airbnb style)
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { handleTabClick(tab) },
                                icon = {
                                    if (tab.route == ROUTE_MESSAGES && isSignedIn && unreadNotifCount > 0) {
                                        BadgedBox(badge = { Badge() }) {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label,
                                                modifier = Modifier.size(DiajakIconSize.Primary)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label,
                                            modifier = Modifier.size(DiajakIconSize.Primary)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(ROUTE_HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onActivityClick = { activity ->
                        navController.navigate("detail/${activity.id}")
                    },
                    onOpenProfile = {
                        if (isSignedIn) {
                            navigateToTopTab(ROUTE_PROFILE)
                        } else {
                            requestAuth(
                                "Masuk dengan Google untuk mengakses Profil & Portal Penyelenggara.",
                                PendingAuthAction.OpenTab(ROUTE_PROFILE)
                            )
                        }
                    },
                    onOpenNotifications = {
                        if (isSignedIn) {
                            messagesInitialTab = 2
                            navigateToTopTab(ROUTE_MESSAGES)
                        } else {
                            requestAuth(
                                "Masuk dengan Google untuk melihat notifikasi aktivitas & tiketmu.",
                                PendingAuthAction.OpenTab(ROUTE_MESSAGES, messagesTab = 2)
                            )
                        }
                    },
                    onToggleFavorite = handleToggleFavorite
                )
            }

            composable(ROUTE_LOGIN) {
                AuthScreen(
                    onAuthSuccess = {},
                    reasonMessage = authPromptReason,
                    onDismiss = {
                        pendingAuthAction = null
                        authPromptReason = null
                        navController.popBackStack()
                    }
                )
            }

            composable(ROUTE_FAVORITES) {
                FavoritesScreen(
                    viewModel = viewModel,
                    onActivityClick = { activity ->
                        navController.navigate("detail/${activity.id}")
                    },
                    onExploreClick = { navigateToTopTab(ROUTE_HOME) }
                )
            }

            composable(ROUTE_TICKETS) {
                TicketsScreen(
                    viewModel = viewModel,
                    onExploreClick = { navigateToTopTab(ROUTE_HOME) },
                    onOpenGroupChat = { groupId ->
                        val exists = (conversationsState as? UiState.Success)?.data?.any { it.id == groupId } == true
                        if (exists) {
                            navController.navigate("chat/$groupId")
                        } else {
                            messagesInitialTab = 1
                            navigateToTopTab(ROUTE_MESSAGES)
                        }
                    }
                )
            }

            composable(ROUTE_MESSAGES) {
                MessagesScreen(
                    viewModel = viewModel,
                    initialTab = messagesInitialTab,
                    onOpenConversation = { convId ->
                        navController.navigate("chat/$convId")
                    }
                )
            }

            composable(ROUTE_PROFILE) {
                ProfileScreen(
                    viewModel = viewModel,
                    onOpenOrganizerPortal = {
                        navController.navigate(ROUTE_ORGANIZER_PORTAL)
                    }
                )
            }

            composable(ROUTE_DETAIL) { entry ->
                val activityId = entry.arguments?.getString("activityId").orEmpty()
                val allActs = ((publishedState as? UiState.Success)?.data ?: emptyList()) +
                    ((organizerActivitiesState as? UiState.Success)?.data ?: emptyList())
                val activity = allActs.firstOrNull { it.id == activityId }
                if (activity != null) {
                    ActivityDetailScreen(
                        activity = activity,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onBookActivity = { target ->
                            if (isSignedIn) {
                                navController.navigate("booking/${target.id}")
                            } else {
                                requestAuth(
                                    "Masuk dengan Google untuk melanjutkan booking & menerbitkan E-Tiket.",
                                    PendingAuthAction.OpenBooking(target.id)
                                )
                            }
                        },
                        onToggleFavorite = handleToggleFavorite,
                        onChatOrganizer = { target ->
                            if (isSignedIn) {
                                viewModel.openDirectChatWithOrganizer(target) { convId ->
                                    navController.navigate("chat/$convId")
                                }
                            } else {
                                requestAuth(
                                    "Masuk dengan Google untuk chat langsung dengan penyelenggara.",
                                    PendingAuthAction.OpenOrganizerChat(target)
                                )
                            }
                        },
                        onRequireAuthForReview = {
                            requestAuth(
                                "Masuk dengan Google untuk menulis ulasan aktivitas ini.",
                                PendingAuthAction.StayOnCurrentScreen
                            )
                        }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aktivitas tidak ditemukan.")
                    }
                }
            }

            composable(ROUTE_BOOKING) { entry ->
                val activityId = entry.arguments?.getString("activityId").orEmpty()
                val allActs = ((publishedState as? UiState.Success)?.data ?: emptyList()) +
                    ((organizerActivitiesState as? UiState.Success)?.data ?: emptyList())
                val activity = allActs.firstOrNull { it.id == activityId }
                if (activity != null) {
                    BookingCheckoutScreen(
                        activity = activity,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onGoToTickets = {
                            navController.popBackStack(ROUTE_HOME, inclusive = false)
                            navigateToTopTab(ROUTE_TICKETS)
                        },
                        onGoToMessages = {
                            messagesInitialTab = 1
                            navController.popBackStack(ROUTE_HOME, inclusive = false)
                            navigateToTopTab(ROUTE_MESSAGES)
                        }
                    )
                }
            }

            composable(ROUTE_CHAT_ROOM) { entry ->
                val convId = entry.arguments?.getString("conversationId").orEmpty()
                val conv = (conversationsState as? UiState.Success)?.data?.firstOrNull { it.id == convId }
                if (conv != null) {
                    ChatRoomScreen(
                        conversation = conv,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Memuat ruang percakapan...")
                    }
                }
            }

            // ORGANIZER PORTAL & ALL 6 MODULES
            composable(ROUTE_ORGANIZER_PORTAL) {
                OrganizerPortalScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenModule = { module -> navController.navigate(module.route) }
                )
            }

            composable(OrganizerModuleRoute.ACTIVITIES.route) {
                ManageActivitiesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onGoToDocuments = { navController.navigate(OrganizerModuleRoute.DOCUMENTS.route) }
                )
            }

            composable(OrganizerModuleRoute.REGISTRATIONS.route) {
                ManageRegistrationsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(OrganizerModuleRoute.MESSAGES.route) {
                ManageOrganizerMessagesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenConversation = { convId -> navController.navigate("chat/$convId") }
                )
            }

            composable(OrganizerModuleRoute.BALANCE.route) {
                ManageBalanceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(OrganizerModuleRoute.REVIEWS.route) {
                ManageReviewsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(OrganizerModuleRoute.DOCUMENTS.route) {
                ManageDocumentsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
