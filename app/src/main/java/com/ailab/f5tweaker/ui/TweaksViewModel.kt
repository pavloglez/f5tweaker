package com.ailab.f5tweaker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ailab.f5tweaker.shizuku.CommandRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TweaksState(
    val isShizukuAvailable: Boolean = false,
    val isShizukuAuthorized: Boolean = false,
    
    val masterProfile: String = "Custom", // Battery, Balanced, Performance, Custom
    
    val refreshRateProfile: String = "Balanced",
    val performanceProfile: String = "Balanced",
    val networkProfile: String = "Balanced",
    val appsRamProfile: String = "Balanced",
    val animationProfile: String = "Balanced",
    val miscProfile: String = "Balanced",
    val touchBoostEnabled: Boolean = false,
    
    val joyoseDisabled: Boolean = false,
    val debloatEnabled: Boolean = false,
    val multicorePacketEnabled: Boolean = false,
    val telemetryDisabled: Boolean = false,
    val skiaVkEnabled: Boolean = false,
    val dozeDisabled: Boolean = false,
    val resizableWindowsEnabled: Boolean = false,
    val harmanKardonEnabled: Boolean = false,
    val speedModeEnabled: Boolean = false
)

class TweaksViewModel(private val commandRunner: CommandRunner) : ViewModel() {

    private val _uiState = MutableStateFlow(TweaksState())
    val uiState: StateFlow<TweaksState> = _uiState.asStateFlow()

    fun updateShizukuStatus(available: Boolean, authorized: Boolean) {
        val wasAuthorized = _uiState.value.isShizukuAuthorized
        _uiState.update { it.copy(isShizukuAvailable = available, isShizukuAuthorized = authorized) }
        
        if (authorized && !wasAuthorized) {
            fetchSystemState()
        }
    }

    private fun fetchSystemState() {
        viewModelScope.launch {
            val peakRefresh = commandRunner.execute("settings get system peak_refresh_rate").getOrNull()?.trim()
            val touchBoost = commandRunner.execute("settings get system touch_response_boost").getOrNull()?.trim()
            val cpuBoost = commandRunner.execute("settings get global cpu_boost").getOrNull()?.trim()
            val netBoost = commandRunner.execute("settings get global mobile_data_boost").getOrNull()?.trim()
            val animScale = commandRunner.execute("settings get global animator_duration_scale").getOrNull()?.trim()
            val appBoost = commandRunner.execute("settings get secure app_launch_boost").getOrNull()?.trim()
            val vsync = commandRunner.execute("settings get system hwui_disable_vsync").getOrNull()?.trim()

            val joyoseState = commandRunner.execute("pm list packages --user 0 com.xiaomi.joyose").getOrNull()
            val analyticsState = commandRunner.execute("pm list packages --user 0 com.miui.analytics").getOrNull()
            val multicore = commandRunner.execute("settings get system multicore_packet_scheduler").getOrNull()?.trim()
            val telemetry = commandRunner.execute("settings get system rakuten_denwa").getOrNull()?.trim()
            val skiavk = commandRunner.execute("getprop debug.hwui.renderer").getOrNull()?.trim()
            val doze = commandRunner.execute("settings get secure doze_mode").getOrNull()?.trim()
            val resizable = commandRunner.execute("settings get global force_resizable_activities").getOrNull()?.trim()
            val harman = commandRunner.execute("settings get system settings_system_harman_kardon_enable").getOrNull()?.trim()
            val speedMode = commandRunner.execute("settings get secure speed_mode_enable").getOrNull()?.trim()

            val refresh = when(peakRefresh) { "120" -> "Performance"; "60" -> "Battery"; else -> "Balanced" }
            val perf = when(cpuBoost) { "1" -> "Performance"; "0" -> "Battery"; else -> "Balanced" }
            val net = when(netBoost) { "1" -> "Performance"; "0" -> "Battery"; else -> "Balanced" }
            val anim = when(animScale) { "0.8" -> "Performance"; "0.5" -> "Battery"; else -> "Balanced" }
            val apps = when(appBoost) { "1" -> "Performance"; "0" -> "Battery"; else -> "Balanced" }
            val misc = when(vsync) { "0" -> "Performance"; "1" -> "Battery"; else -> "Balanced" }

            val master = if (refresh == perf && perf == net && net == anim && anim == apps && apps == misc) refresh else "Custom"

            _uiState.update {
                it.copy(
                    masterProfile = master,
                    refreshRateProfile = refresh,
                    performanceProfile = perf,
                    networkProfile = net,
                    animationProfile = anim,
                    appsRamProfile = apps,
                    miscProfile = misc,
                    touchBoostEnabled = touchBoost == "1",
                    joyoseDisabled = joyoseState?.contains("com.xiaomi.joyose") == false,
                    debloatEnabled = analyticsState?.contains("com.miui.analytics") == false,
                    multicorePacketEnabled = multicore == "1",
                    telemetryDisabled = telemetry == "0",
                    skiaVkEnabled = skiavk == "skiavk",
                    dozeDisabled = doze == "0" || doze == "null", // It's 1 in battery mode
                    resizableWindowsEnabled = resizable == "1",
                    harmanKardonEnabled = harman == "1",
                    speedModeEnabled = speedMode == "1"
                )
            }
        }
    }

    private fun checkMasterProfile() {
        _uiState.update { state ->
            val profiles = listOf(
                state.refreshRateProfile, state.performanceProfile, 
                state.networkProfile, state.animationProfile, 
                state.appsRamProfile, state.miscProfile
            )
            val isAllSame = profiles.all { it == profiles.first() }
            if (isAllSame) {
                state.copy(masterProfile = profiles.first())
            } else {
                state.copy(masterProfile = "Custom")
            }
        }
    }

    fun setMasterProfile(profile: String) {
        if (profile == "Custom") return
        _uiState.update { 
            it.copy(
                masterProfile = profile,
                refreshRateProfile = profile,
                performanceProfile = profile,
                networkProfile = profile,
                animationProfile = profile,
                appsRamProfile = profile,
                miscProfile = profile,
                touchBoostEnabled = profile != "Battery", // Boost is 1 for Balanced and Performance, 0 for Battery
                dozeDisabled = profile != "Battery" // Doze is 1 for Battery, disabled otherwise
            )
        }
        
        setRefreshRateProfile(profile, skipCheck = true)
        setPerformanceProfile(profile, skipCheck = true)
        setNetworkProfile(profile, skipCheck = true)
        setAnimationProfile(profile, skipCheck = true)
        setAppsRamProfile(profile, skipCheck = true)
        setMiscProfile(profile, skipCheck = true)
        toggleTouchBoost(profile != "Battery", skipCheck = true)
        toggleDoze(profile != "Battery", skipCheck = true)
    }

    fun setRefreshRateProfile(profile: String, skipCheck: Boolean = false) {
        _uiState.update { it.copy(refreshRateProfile = profile) }
        viewModelScope.launch {
            when (profile) {
                "Performance" -> {
                    commandRunner.execute("settings put system peak_refresh_rate 120")
                    commandRunner.execute("settings put system min_refresh_rate 120")
                    commandRunner.execute("settings put global refresh_rate_mode 0")
                    commandRunner.execute("settings put secure miui_refresh_rate 120")
                    commandRunner.execute("settings put secure user_refresh_rate 120")
                }
                "Balanced" -> {
                    commandRunner.execute("settings put system peak_refresh_rate 90")
                    commandRunner.execute("settings put system min_refresh_rate 90")
                    commandRunner.execute("settings put global refresh_rate_mode 0")
                    commandRunner.execute("settings put global low_power_refresh_rate 90")
                    commandRunner.execute("settings put secure miui_refresh_rate 90")
                    commandRunner.execute("settings put secure user_refresh_rate 90")
                }
                "Battery" -> {
                    commandRunner.execute("settings put system peak_refresh_rate 60")
                    commandRunner.execute("settings put system min_refresh_rate 60")
                    commandRunner.execute("settings put global refresh_rate_mode 0")
                    commandRunner.execute("settings put global low_power_refresh_rate 60")
                    commandRunner.execute("settings put secure miui_refresh_rate 60")
                    commandRunner.execute("settings put secure user_refresh_rate 60")
                }
            }
        }
        if (!skipCheck) checkMasterProfile()
    }

    fun setPerformanceProfile(profile: String, skipCheck: Boolean = false) {
        _uiState.update { it.copy(performanceProfile = profile) }
        viewModelScope.launch {
            when (profile) {
                "Performance" -> {
                    commandRunner.execute("settings put global cpu_boost 1")
                    commandRunner.execute("settings put global gpu_boost 1")
                    commandRunner.execute("settings put global thermal_limit 1")
                    commandRunner.execute("settings put global scheduler_policy performance")
                }
                "Balanced" -> {
                    commandRunner.execute("settings put global cpu_boost 1")
                    commandRunner.execute("settings put global gpu_boost 1")
                    commandRunner.execute("settings put global thermal_limit 2")
                    commandRunner.execute("settings put global scheduler_policy balanced")
                }
                "Battery" -> {
                    commandRunner.execute("settings put global cpu_boost 0")
                    commandRunner.execute("settings put global gpu_boost 0")
                    commandRunner.execute("settings put global thermal_limit 3")
                    commandRunner.execute("settings put global scheduler_policy balanced")
                }
            }
        }
        if (!skipCheck) checkMasterProfile()
    }

    fun setNetworkProfile(profile: String, skipCheck: Boolean = false) {
        _uiState.update { it.copy(networkProfile = profile) }
        viewModelScope.launch {
            when (profile) {
                "Performance", "Balanced" -> {
                    commandRunner.execute("settings put global wifi_scan_optimization 1")
                    commandRunner.execute("settings put global mobile_data_boost 1")
                    commandRunner.execute("settings put global network_latency_opt 1")
                }
                "Battery" -> {
                    commandRunner.execute("settings put global wifi_scan_optimization 1")
                    commandRunner.execute("settings put global mobile_data_boost 0")
                    commandRunner.execute("settings put global network_latency_opt 0")
                }
            }
        }
        if (!skipCheck) checkMasterProfile()
    }

    fun setAnimationProfile(profile: String, skipCheck: Boolean = false) {
        _uiState.update { it.copy(animationProfile = profile) }
        viewModelScope.launch {
            val scale = when (profile) {
                "Performance" -> "0.8"
                "Balanced" -> "0.7"
                "Battery" -> "0.5"
                else -> "1.0"
            }
            commandRunner.execute("settings put global animator_duration_scale \$scale")
            commandRunner.execute("settings put global transition_animation_scale \$scale")
            commandRunner.execute("settings put global window_animation_scale \$scale")
        }
        if (!skipCheck) checkMasterProfile()
    }

    fun setAppsRamProfile(profile: String, skipCheck: Boolean = false) {
        _uiState.update { it.copy(appsRamProfile = profile) }
        viewModelScope.launch {
            when (profile) {
                "Performance" -> {
                    commandRunner.execute("settings put secure app_launch_boost 1")
                    commandRunner.execute("settings put secure zygote_prefork 1")
                    commandRunner.execute("settings put secure force_app_standby 0")
                    commandRunner.execute("settings put secure background_app_limit 0")
                    commandRunner.execute("settings put secure keep_launcher_in_memory 1")
                    commandRunner.execute("settings put secure ram_boost 1")
                }
                "Balanced" -> {
                    commandRunner.execute("settings put secure app_launch_boost 1")
                    commandRunner.execute("settings put secure zygote_prefork 1")
                    commandRunner.execute("settings put secure force_app_standby 0")
                    commandRunner.execute("settings put secure background_app_limit 1")
                    commandRunner.execute("settings put secure keep_launcher_in_memory 1")
                    commandRunner.execute("settings put secure ram_boost 1")
                }
                "Battery" -> {
                    commandRunner.execute("settings put secure app_launch_boost 0")
                    commandRunner.execute("settings put secure zygote_prefork 0")
                    commandRunner.execute("settings put secure force_app_standby 1")
                    commandRunner.execute("settings put secure background_app_limit 3")
                    commandRunner.execute("settings put secure keep_launcher_in_memory 0")
                    commandRunner.execute("settings put secure ram_boost 0")
                }
            }
        }
        if (!skipCheck) checkMasterProfile()
    }

    fun setMiscProfile(profile: String, skipCheck: Boolean = false) {
        _uiState.update { it.copy(miscProfile = profile) }
        viewModelScope.launch {
            when (profile) {
                "Performance" -> {
                    commandRunner.execute("settings put system hwui_disable_vsync 0")
                    commandRunner.execute("settings put global surfaceflinger_max_frame_interval 1")
                    commandRunner.execute("settings put global dalvik.vm.heapsize 384m")
                }
                "Balanced" -> {
                    commandRunner.execute("settings put system hwui_disable_vsync 0")
                    commandRunner.execute("settings put global surfaceflinger_max_frame_interval 1")
                    commandRunner.execute("settings put global dalvik.vm.heapsize 320m")
                    commandRunner.execute("settings put global dalvik.vm.heapgrowthlimit 192m")
                }
                "Battery" -> {
                    commandRunner.execute("settings put system hwui_disable_vsync 1")
                    commandRunner.execute("settings put global surfaceflinger_max_frame_interval 2")
                    commandRunner.execute("settings put global dalvik.vm.heapsize 256m")
                    commandRunner.execute("settings put global dalvik.vm.heapgrowthlimit 128m")
                }
            }
        }
        if (!skipCheck) checkMasterProfile()
    }

    fun toggleTouchBoost(enabled: Boolean, skipCheck: Boolean = false) {
        _uiState.update { it.copy(touchBoostEnabled = enabled) }
        viewModelScope.launch {
            if (enabled) {
                commandRunner.execute("settings put system touch_response_boost 1")
                commandRunner.execute("settings put system touch_latency 0")
            } else {
                commandRunner.execute("settings put system touch_response_boost 0")
                commandRunner.execute("settings put system touch_latency 1")
            }
        }
        if (!skipCheck) _uiState.update { it.copy(masterProfile = "Custom") }
    }

    fun toggleDoze(disabled: Boolean, skipCheck: Boolean = false) {
        _uiState.update { it.copy(dozeDisabled = disabled) }
        viewModelScope.launch {
            val v = if (disabled) "0" else "1"
            commandRunner.execute("settings put secure doze_mode \$v")
            if (disabled) {
                commandRunner.execute("dumpsys deviceidle disable")
            } else {
                commandRunner.execute("dumpsys deviceidle enable")
            }
        }
        if (!skipCheck) _uiState.update { it.copy(masterProfile = "Custom") }
    }

    fun toggleJoyose(disabled: Boolean) {
        _uiState.update { it.copy(joyoseDisabled = disabled) }
        viewModelScope.launch {
            if (disabled) {
                commandRunner.execute("pm uninstall --user 0 com.xiaomi.joyose")
            } else {
                commandRunner.execute("pm install-existing --user 0 com.xiaomi.joyose")
            }
        }
    }

    fun toggleDebloat(enabled: Boolean) {
        _uiState.update { it.copy(debloatEnabled = enabled) }
        viewModelScope.launch {
            val packages = listOf(
                "com.miui.analytics",
                "com.miui.daemon",
                "com.miui.msa.global",
                "com.miui.systemAdSolution",
                "com.miui.android.fashiongallery",
                "com.xiaomi.mipicks",
                "com.mi.globalbrowser",
                "com.miui.bugreport",
                "com.miui.extraphoto",
                "com.xiaomi.payment",
                "com.miui.yellowpage"
            )
            if (enabled) {
                packages.forEach { commandRunner.execute("pm uninstall --user 0 \$it") }
            } else {
                packages.forEach { commandRunner.execute("pm install-existing --user 0 \$it") }
            }
        }
    }

    fun toggleMulticorePacket(enabled: Boolean) {
        _uiState.update { it.copy(multicorePacketEnabled = enabled) }
        viewModelScope.launch {
            commandRunner.execute("settings put system multicore_packet_scheduler \${if (enabled) 1 else 0}")
        }
    }

    fun toggleTelemetry(disabled: Boolean) {
        _uiState.update { it.copy(telemetryDisabled = disabled) }
        viewModelScope.launch {
            val v = if (disabled) "0" else "1"
            commandRunner.execute("settings put system rakuten_denwa \$v")
            commandRunner.execute("settings put system send_security_reports \$v")
            commandRunner.execute("settings put secure send_action_app_error \$v")
            commandRunner.execute("settings put global activity_starts_logging_enabled \$v")
        }
    }

    fun toggleSkiaVk(enabled: Boolean) {
        _uiState.update { it.copy(skiaVkEnabled = enabled) }
        viewModelScope.launch {
            if (enabled) {
                commandRunner.execute("setprop debug.hwui.renderer skiavk")
            } else {
                commandRunner.execute("setprop debug.hwui.renderer opengl")
            }
        }
    }

    fun toggleResizableWindows(enabled: Boolean) {
        _uiState.update { it.copy(resizableWindowsEnabled = enabled) }
        viewModelScope.launch {
            commandRunner.execute("settings put global force_resizable_activities \${if (enabled) 1 else 0}")
        }
    }

    fun toggleHarmanKardon(enabled: Boolean) {
        _uiState.update { it.copy(harmanKardonEnabled = enabled) }
        viewModelScope.launch {
            commandRunner.execute("settings put system settings_system_harman_kardon_enable \${if (enabled) 1 else 0}")
        }
    }

    fun toggleSpeedMode(enabled: Boolean) {
        _uiState.update { it.copy(speedModeEnabled = enabled) }
        viewModelScope.launch {
            commandRunner.execute("settings put secure speed_mode_enable \${if (enabled) 1 else 0}")
        }
    }
}
