package com.example.device

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryStatus(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val powerSource: String = "Battery",
    val health: String = "Good",
    val temperatureCelsius: Float = 28.0f
)

data class StorageInfo(
    val availableGb: Double = 0.0,
    val totalGb: Double = 0.0,
    val usedPercentage: Int = 0
)

class DeviceActionManager(private val context: Context) : SensorEventListener {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _compassAzimuth = MutableStateFlow(0f)
    val compassAzimuth: StateFlow<Float> = _compassAzimuth.asStateFlow()

    private var gravityValues = FloatArray(3)
    private var geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    init {
        registerTorchCallback()
        startSensors()
    }

    private fun registerTorchCallback() {
        cameraManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                super.onTorchModeChanged(cameraId, enabled)
                _isFlashlightOn.value = enabled
            }
        }, null)
    }

    fun toggleFlashlight(): Result<Boolean> {
        val manager = cameraManager ?: return Result.failure(Exception("Camera manager unavailable"))
        return try {
            val cameraId = manager.cameraIdList.firstOrNull { id ->
                val chars = manager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                        chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList.firstOrNull() ?: return Result.failure(Exception("No camera flash found"))

            val newState = !_isFlashlightOn.value
            manager.setTorchMode(cameraId, newState)
            _isFlashlightOn.value = newState
            Result.success(newState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setFlashlight(enabled: Boolean): Result<Boolean> {
        val manager = cameraManager ?: return Result.failure(Exception("Camera manager unavailable"))
        return try {
            val cameraId = manager.cameraIdList.firstOrNull { id ->
                val chars = manager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                        chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList.firstOrNull() ?: return Result.failure(Exception("No camera flash found"))

            manager.setTorchMode(cameraId, enabled)
            _isFlashlightOn.value = enabled
            Result.success(enabled)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBatteryStatus(): BatteryStatus {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val powerSource = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
            else -> if (isCharging) "Charging" else "Battery"
        }

        val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250) ?: 250
        val tempCelsius = tempRaw / 10.0f

        return BatteryStatus(
            percentage = batteryPct,
            isCharging = isCharging,
            powerSource = powerSource,
            health = "Normal",
            temperatureCelsius = tempCelsius
        )
    }

    fun getStorageInfo(): StorageInfo {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - availableBytes

            val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val availableGb = availableBytes / (1024.0 * 1024.0 * 1024.0)
            val usedPct = if (totalBytes > 0) ((usedBytes * 100) / totalBytes).toInt() else 0

            StorageInfo(availableGb = availableGb, totalGb = totalGb, usedPercentage = usedPct)
        } catch (e: Exception) {
            StorageInfo(availableGb = 32.0, totalGb = 64.0, usedPercentage = 50)
        }
    }

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun adjustVolume(increase: Boolean): Int {
        val am = audioManager ?: return 50
        val flag = AudioManager.FLAG_SHOW_UI
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, flag)
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return if (max > 0) (current * 100 / max) else 50
    }

    fun copyToClipboard(label: String, text: String) {
        val clip = ClipData.newPlainText(label, text)
        clipboardManager?.setPrimaryClip(clip)
    }

    fun readClipboard(): String {
        val clip = clipboardManager?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text
            if (!text.isNullOrEmpty()) {
                return text.toString()
            }
        }
        return ""
    }

    fun launchAppByName(query: String): Result<String> {
        val pm = context.packageManager
        val queryLower = query.lowercase().trim()

        val knownPackages = mapOf(
            "chrome" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "camera" to "camera",
            "calculator" to "calculator",
            "clock" to "clock",
            "maps" to "com.google.android.apps.maps",
            "settings" to "com.android.settings",
            "play store" to "com.android.vending",
            "gmail" to "com.google.android.gm"
        )

        for ((key, pkg) in knownPackages) {
            if (queryLower.contains(key)) {
                if (pkg == "camera") {
                    val intent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                        return Result.success("Opening Camera")
                    }
                }
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return Result.success("Opening ${key.replaceFirstChar { it.uppercase() }}")
                }
            }
        }

        // General search in installed apps
        val installedApps = pm.getInstalledApplications(0)
        for (app in installedApps) {
            val appLabel = pm.getApplicationLabel(app).toString().lowercase()
            if (appLabel.contains(queryLower) || queryLower.contains(appLabel)) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return Result.success("Opening ${pm.getApplicationLabel(app)}")
                }
            }
        }

        return Result.failure(Exception("Application matching '$query' was not found on this device."))
    }

    fun setAlarm(hour: Int, minutes: Int, message: String): Result<String> {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            Result.success("Alarm interface opened for ${String.format("%02d:%02d", hour, minutes)}")
        } catch (e: Exception) {
            Result.failure(Exception("Unable to open alarm interface: ${e.message}"))
        }
    }

    fun setTimer(seconds: Int, message: String): Result<String> {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            Result.success("Timer started for $seconds seconds")
        } catch (e: Exception) {
            Result.failure(Exception("Timer interface not supported: ${e.message}"))
        }
    }

    fun openDialer(phoneNumber: String): Result<String> {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            Result.success("Dialer opened with $phoneNumber")
        } catch (e: Exception) {
            Result.failure(Exception("Could not launch phone dialer: ${e.message}"))
        }
    }

    fun openSettings(action: String): Result<String> {
        val target = when (action.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "sound" -> Settings.ACTION_SOUND_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "apps" -> Settings.ACTION_APPLICATION_SETTINGS
            "date" -> Settings.ACTION_DATE_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(target).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            Result.success("Opened $action settings")
        } catch (e: Exception) {
            Result.failure(Exception("Could not open settings: ${e.message}"))
        }
    }

    fun shareText(text: String, title: String = "Share via JARVIS PRO") {
        val sendIntent = Intent().apply {
            this.action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, title).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(shareIntent)
    }

    fun startSensors() {
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopSensors() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravityValues, geomagneticValues)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                val azimuthInRadians = orientation[0]
                val azimuthInDegrees = (Math.toDegrees(azimuthInRadians.toDouble()).toFloat() + 360) % 360
                _compassAzimuth.value = azimuthInDegrees
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
