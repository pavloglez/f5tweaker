package com.ailab.f5tweaker

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ailab.f5tweaker.shizuku.ShizukuCommandRunner
import com.ailab.f5tweaker.theme.*
import com.ailab.f5tweaker.ui.TweaksState
import com.ailab.f5tweaker.ui.TweaksViewModel
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TweaksViewModel(ShizukuCommandRunner()) as T
            }
        }

        setContent {
            F5TweakerTheme {
                val viewModel: TweaksViewModel = viewModel(factory = viewModelFactory)
                val uiState by viewModel.uiState.collectAsState()

                DisposableEffect(Unit) {
                    val listener = Shizuku.OnBinderReceivedListener {
                        checkShizukuState(viewModel)
                    }
                    val deadListener = Shizuku.OnBinderDeadListener {
                        checkShizukuState(viewModel)
                    }
                    val requestListener = Shizuku.OnRequestPermissionResultListener { requestCode, _ ->
                        if (requestCode == 1) {
                            checkShizukuState(viewModel)
                        }
                    }

                    Shizuku.addBinderReceivedListenerSticky(listener)
                    Shizuku.addBinderDeadListener(deadListener)
                    Shizuku.addRequestPermissionResultListener(requestListener)

                    checkShizukuState(viewModel)

                    onDispose {
                        Shizuku.removeBinderReceivedListener(listener)
                        Shizuku.removeBinderDeadListener(deadListener)
                        Shizuku.removeRequestPermissionResultListener(requestListener)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TweaksScreen(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }

    private fun checkShizukuState(viewModel: TweaksViewModel) {
        val isAvailable = Shizuku.pingBinder()
        var isAuthorized = false
        if (isAvailable) {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                isAuthorized = true
            } else if (!Shizuku.shouldShowRequestPermissionRationale()) {
                Shizuku.requestPermission(1)
            }
        }
        viewModel.updateShizukuStatus(isAvailable, isAuthorized)
    }
}

enum class TweakCategory(val title: String) {
    ALL("All"),
    PRESET("Master"),
    PERFORMANCE("Performance"),
    DISPLAY("Display"),
    SYSTEM("System")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweaksScreen(
    uiState: TweaksState,
    viewModel: TweaksViewModel
) {
    var selectedCategory by remember { mutableStateOf(TweakCategory.ALL) }
    var showJoyoseWarningDialog by remember { mutableStateOf(false) }

    if (showJoyoseWarningDialog) {
        AlertDialog(
            onDismissRequest = { showJoyoseWarningDialog = false },
            title = {
                Text(
                    text = "Disable Thermal Throttling?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Disabling Joyose removes MIUI thermal guardrails. Hardware temperatures may rise significantly during intensive workloads. An active cooling fan is strongly recommended.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleJoyose(true)
                        showJoyoseWarningDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Disable Joyose")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoyoseWarningDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Poco F5 Tweaker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hardware & Kernel Tuning (marble)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    ShizukuStatusPill(
                        isAvailable = uiState.isShizukuAvailable,
                        isAuthorized = uiState.isShizukuAuthorized
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shizuku Notice Banner if unavailable/unauthorized
            if (!uiState.isShizukuAvailable || !uiState.isShizukuAuthorized) {
                item {
                    ShizukuBannerCard(
                        isAvailable = uiState.isShizukuAvailable,
                        isAuthorized = uiState.isShizukuAuthorized,
                        onRequestPermission = { Shizuku.requestPermission(1) }
                    )
                }
            }

            if (uiState.isShizukuAvailable && uiState.isShizukuAuthorized) {
                // Category Filter Tabs
                item {
                    CategorySelector(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }

                // Master Preset Section
                if (selectedCategory == TweakCategory.ALL || selectedCategory == TweakCategory.PRESET) {
                    item {
                        SectionHeader(title = "GLOBAL PRESET")
                        Spacer(modifier = Modifier.height(6.dp))
                        MasterProfileCard(
                            currentProfile = uiState.masterProfile,
                            onProfileSelected = { viewModel.setMasterProfile(it) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionFooter(text = "Applies unified refresh rate, scheduler, data boost, and doze configuration in one tap.")
                    }
                }

                // Performance & Display Category
                if (selectedCategory == TweakCategory.ALL || selectedCategory == TweakCategory.PERFORMANCE || selectedCategory == TweakCategory.DISPLAY) {
                    item {
                        SectionHeader(title = "PERFORMANCE & DISPLAY")
                        Spacer(modifier = Modifier.height(6.dp))
                        InsetGroupedCard {
                            GroupedSegmentedRow(
                                icon = Icons.Rounded.Refresh,
                                iconColor = SystemBlueLight,
                                title = "Display Refresh Rate",
                                description = "Limit to 60Hz for battery, dynamic 90Hz, or locked 120Hz.",
                                options = listOf("Battery", "Balanced", "Performance"),
                                selectedOption = uiState.refreshRateProfile,
                                onOptionSelected = { viewModel.setRefreshRateProfile(it) }
                            )
                            GroupedDivider()
                            GroupedSegmentedRow(
                                icon = Icons.Rounded.Speed,
                                iconColor = SystemOrangeLight,
                                title = "CPU & GPU Power",
                                description = "Tuning governors, thermal limiters, and scheduler boosts.",
                                options = listOf("Battery", "Balanced", "Performance"),
                                selectedOption = uiState.performanceProfile,
                                onOptionSelected = { viewModel.setPerformanceProfile(it) }
                            )
                            GroupedDivider()
                            GroupedSegmentedRow(
                                icon = Icons.Rounded.Wifi,
                                iconColor = SystemGreenLight,
                                title = "Network & Connectivity",
                                description = "Wi-Fi scan optimization and packet latency reductions.",
                                options = listOf("Battery", "Balanced", "Performance"),
                                selectedOption = uiState.networkProfile,
                                onOptionSelected = { viewModel.setNetworkProfile(it) }
                            )
                            GroupedDivider()
                            GroupedSegmentedRow(
                                icon = Icons.Rounded.Animation,
                                iconColor = SystemPurpleLight,
                                title = "Animations Scale",
                                description = "Adjust animator and window transitions (0.5x, 0.7x, 0.8x).",
                                options = listOf("Battery", "Balanced", "Performance"),
                                selectedOption = uiState.animationProfile,
                                onOptionSelected = { viewModel.setAnimationProfile(it) }
                            )
                            GroupedDivider()
                            GroupedSegmentedRow(
                                icon = Icons.Rounded.Memory,
                                iconColor = SystemIndigoLight,
                                title = "Apps & Memory Prefork",
                                description = "Zygote prefork and background task memory thresholds.",
                                options = listOf("Battery", "Balanced", "Performance"),
                                selectedOption = uiState.appsRamProfile,
                                onOptionSelected = { viewModel.setAppsRamProfile(it) }
                            )
                            GroupedDivider()
                            GroupedSegmentedRow(
                                icon = Icons.Rounded.SettingsSuggest,
                                iconColor = SystemBlueLight,
                                title = "SurfaceFlinger & Dalvik",
                                description = "Vsync intervals, Dalvik heap limits (256m–384m).",
                                options = listOf("Battery", "Balanced", "Performance"),
                                selectedOption = uiState.miscProfile,
                                onOptionSelected = { viewModel.setMiscProfile(it) }
                            )
                        }
                    }
                }

                // Granular Hardware & System Toggles
                if (selectedCategory == TweakCategory.ALL || selectedCategory == TweakCategory.PERFORMANCE || selectedCategory == TweakCategory.SYSTEM) {
                    item {
                        SectionHeader(title = "HARDWARE & SYSTEM TOGGLES")
                        Spacer(modifier = Modifier.height(6.dp))
                        InsetGroupedCard {
                            GroupedSwitchRow(
                                icon = Icons.Rounded.TouchApp,
                                iconColor = SystemBlueLight,
                                title = "Touch Responsiveness Boost",
                                description = "Increases touch digitizer sampling rate to eliminate input lag.",
                                checked = uiState.touchBoostEnabled,
                                onCheckedChange = { viewModel.toggleTouchBoost(it) }
                            )
                            GroupedDivider()
                            GroupedSwitchRow(
                                icon = Icons.Rounded.BatteryChargingFull,
                                iconColor = SystemOrangeLight,
                                title = "Disable Doze Standby",
                                description = "Prevents device from sleeping during idle standby.",
                                checked = uiState.dozeDisabled,
                                onCheckedChange = { viewModel.toggleDoze(it) }
                            )
                            GroupedDivider()
                            GroupedSwitchRow(
                                icon = Icons.Rounded.Warning,
                                iconColor = SystemRedLight,
                                title = "Disable Joyose",
                                description = "Bypasses MIUI thermal throttling. Active cooling strongly recommended.",
                                badge = "Caution",
                                badgeColor = SystemRedLight,
                                checked = uiState.joyoseDisabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        showJoyoseWarningDialog = true
                                    } else {
                                        viewModel.toggleJoyose(false)
                                    }
                                }
                            )
                            GroupedDivider()
                            GroupedSwitchRow(
                                icon = Icons.AutoMirrored.Rounded.AltRoute,
                                iconColor = SystemGreenLight,
                                title = "Multicore Packet Scheduler",
                                description = "Distributes network packets over multiple CPU cores for gaming latency.",
                                checked = uiState.multicorePacketEnabled,
                                onCheckedChange = { viewModel.toggleMulticorePacket(it) }
                            )
                            GroupedDivider()
                            GroupedSwitchRow(
                                icon = Icons.Rounded.Security,
                                iconColor = SystemIndigoLight,
                                title = "Disable System Telemetry",
                                description = "Blocks background analytics, bug reports, and error log uploaders.",
                                checked = uiState.telemetryDisabled,
                                onCheckedChange = { viewModel.toggleTelemetry(it) }
                            )
                            GroupedDivider()
                            GroupedSwitchRow(
                                icon = Icons.Rounded.CleaningServices,
                                iconColor = SystemPurpleLight,
                                title = "Debloat MIUI Packages",
                                description = "Removes background ad services, analytics, and MSA components.",
                                checked = uiState.debloatEnabled,
                                onCheckedChange = { viewModel.toggleDebloat(it) }
                            )
                            GroupedDivider()
                            GroupedSwitchRow(
                                icon = Icons.Rounded.ViewInAr,
                                iconColor = SystemBlueLight,
                                title = "Vulkan UI Renderer (SkiaVK)",
                                description = "Accelerates Android rendering via Vulkan backend instead of OpenGL.",
                                checked = uiState.skiaVkEnabled,
                                onCheckedChange = { viewModel.toggleSkiaVk(it) }
                            )
                            GroupedDivider()
                            GroupedSwitchRow(
                                icon = Icons.Rounded.OpenInFull,
                                iconColor = SystemOrangeLight,
                                title = "Force Resizable Windows",
                                description = "Unlocks freeform multi-window resizing for all installed apps.",
                                checked = uiState.resizableWindowsEnabled,
                                onCheckedChange = { viewModel.toggleResizableWindows(it) }
                            )
                            GroupedDivider()
                            GroupedSwitchRow(
                                icon = Icons.Rounded.Headphones,
                                iconColor = SystemPurpleLight,
                                title = "Harman Kardon Audio Engine",
                                description = "Activates enhanced DSP audio profile and dynamic spatial clarity.",
                                checked = uiState.harmanKardonEnabled,
                                onCheckedChange = { viewModel.toggleHarmanKardon(it) }
                            )
                            GroupedDivider()
                            GroupedSwitchRow(
                                icon = Icons.Rounded.RocketLaunch,
                                iconColor = SystemGreenLight,
                                title = "Launcher Speed Mode",
                                description = "Prioritizes animation thread scheduling in MIUI/HyperOS launcher.",
                                checked = uiState.speedModeEnabled,
                                onCheckedChange = { viewModel.toggleSpeedMode(it) }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionFooter(text = "Toggles are applied instantly via Shizuku ADB. System props reset upon full device reboot.")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ShizukuStatusPill(isAvailable: Boolean, isAuthorized: Boolean) {
    val isReady = isAvailable && isAuthorized
    val statusColor = if (isReady) SystemGreenLight else SystemRedLight
    val statusText = if (isReady) "Shizuku Active" else "Disconnected"

    Row(
        modifier = Modifier
            .padding(end = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(statusColor.copy(alpha = 0.12f))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = statusColor
        )
    }
}

@Composable
fun ShizukuBannerCard(
    isAvailable: Boolean,
    isAuthorized: Boolean,
    onRequestPermission: () -> Unit
) {
    val title = when {
        !isAvailable -> "Shizuku Service Not Running"
        !isAuthorized -> "Shizuku Authorization Required"
        else -> "Shizuku Active"
    }

    val body = when {
        !isAvailable -> "Launch the Shizuku app and start the service via Wireless Debugging or Root before applying system tweaks."
        !isAuthorized -> "Permission is required to execute system settings and package management commands without root."
        else -> "Ready to manage system configuration."
    }

    val accent = if (!isAvailable || !isAuthorized) SystemOrangeLight else SystemGreenLight

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(accent.copy(alpha = 0.4f))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (!isAvailable || !isAuthorized) Icons.Rounded.WarningAmber else Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isAvailable && !isAuthorized) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = SystemBlueLight),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Grant Permission",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySelector(
    selectedCategory: TweakCategory,
    onCategorySelected: (TweakCategory) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(TweakCategory.entries.toTypedArray()) { category ->
            val isSelected = category == selectedCategory
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = containerColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCategorySelected(category) }
                    .semantics {
                        role = Role.Tab
                        this.contentDescription = category.title
                    }
            ) {
                Text(
                    text = category.title,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun MasterProfileCard(
    currentProfile: String,
    onProfileSelected: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val profiles = listOf(
        Triple("Battery", "60Hz • Power Saver", SystemGreenLight),
        Triple("Balanced", "90Hz • Dynamic", SystemBlueLight),
        Triple("Performance", "120Hz • Max Boost", SystemOrangeLight)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Master Profile Override",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active: $currentProfile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (currentProfile) {
                        "Performance" -> SystemOrangeLight.copy(alpha = 0.15f)
                        "Battery" -> SystemGreenLight.copy(alpha = 0.15f)
                        "Balanced" -> SystemBlueLight.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = currentProfile.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (currentProfile) {
                            "Performance" -> SystemOrangeLight
                            "Battery" -> SystemGreenLight
                            "Balanced" -> SystemBlueLight
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profiles.forEach { (name, subtitle, accent) ->
                    val isSelected = currentProfile == name
                    val bgCol by animateColorAsState(
                        targetValue = if (isSelected) {
                            accent.copy(alpha = if (isDark) 0.25f else 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        },
                        animationSpec = tween(200),
                        label = "cardBg"
                    )

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onProfileSelected(name) }
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        color = bgCol
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
    )
}

@Composable
fun SectionFooter(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

@Composable
fun InsetGroupedCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun GroupedDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    )
}

@Composable
fun IconSquircleBadge(
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(iconColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupedSegmentedRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconSquircleBadge(icon = icon, iconColor = iconColor)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    modifier = Modifier.weight(1f),
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { onOptionSelected(label) },
                    selected = label == selectedOption,
                    icon = {},
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = Color.Transparent,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (label == selectedOption) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun GroupedSwitchRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badge: String? = null,
    badgeColor: Color = SystemRedLight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconSquircleBadge(icon = icon, iconColor = iconColor)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badge,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SystemGreenLight,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
