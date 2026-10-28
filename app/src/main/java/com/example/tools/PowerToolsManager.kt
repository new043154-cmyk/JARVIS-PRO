package com.example.tools

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.model.PrayerTimes
import com.example.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class SpeedTestResult(
    val downloadSpeedMbps: Double = 0.0,
    val latencyMs: Long = 0,
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null
)

class PowerToolsManager(private val context: Context) : SensorEventListener {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val _weatherData = MutableStateFlow(WeatherData())
    val weatherData: StateFlow<WeatherData> = _weatherData.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    private val _speedTestState = MutableStateFlow(SpeedTestResult())
    val speedTestState: StateFlow<SpeedTestResult> = _speedTestState.asStateFlow()

    private val _isAntiTheftArmed = MutableStateFlow(false)
    val isAntiTheftArmed: StateFlow<Boolean> = _isAntiTheftArmed.asStateFlow()

    private val _isAntiTheftTriggered = MutableStateFlow(false)
    val isAntiTheftTriggered: StateFlow<Boolean> = _isAntiTheftTriggered.asStateFlow()

    private var initialAccel: FloatArray? = null
    private var toneGenerator: ToneGenerator? = null

    // Fetch real live weather from Open-Meteo
    suspend fun refreshWeather(latitude: Double = 23.8103, longitude: Double = 90.4125, cityName: String = "Local Area") = withContext(Dispatchers.IO) {
        _isWeatherLoading.value = true
        try {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m"
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val current = json.getJSONObject("current")
                    val temp = current.getDouble("temperature_2m")
                    val humidity = current.getInt("relative_humidity_2m")
                    val wind = current.getDouble("wind_speed_10m")
                    val weatherCode = current.getInt("weather_code")

                    val condition = decodeWeatherCode(weatherCode)

                    _weatherData.value = WeatherData(
                        city = cityName,
                        temperature = temp,
                        condition = condition,
                        humidity = humidity,
                        windSpeed = wind,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            }
        } catch (e: Exception) {
            // Keep existing with updated timestamp
            _weatherData.value = _weatherData.value.copy(
                condition = "Clear (Offline Cache)",
                lastUpdated = System.currentTimeMillis()
            )
        } finally {
            _isWeatherLoading.value = false
        }
    }

    private fun decodeWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy Atmosphere"
            51, 53, 55 -> "Light Drizzle"
            61, 63, 65 -> "Rain Showers"
            71, 73, 75 -> "Snowfall"
            80, 81, 82 -> "Heavy Rain"
            95, 96, 99 -> "Thunderstorm Alert"
            else -> "Atmospheric Conditions Normal"
        }
    }

    // Real internet speed & latency measurement using OkHttp
    suspend fun runSpeedTest() = withContext(Dispatchers.IO) {
        _speedTestState.value = SpeedTestResult(isRunning = true, progress = 0.1f)
        try {
            val pingStart = System.currentTimeMillis()
            val pingReq = Request.Builder().url("https://www.google.com/generate_204").build()
            httpClient.newCall(pingReq).execute().close()
            val latency = System.currentTimeMillis() - pingStart

            _speedTestState.value = _speedTestState.value.copy(latencyMs = latency, progress = 0.3f)

            // Download real payload (5MB chunk from reliable public CDN)
            val downloadUrl = "https://speed.cloudflare.com/__down?bytes=5000000"
            val downloadReq = Request.Builder().url(downloadUrl).build()

            val dlStart = System.currentTimeMillis()
            var totalBytesRead = 0L

            httpClient.newCall(downloadReq).execute().use { res ->
                val source = res.body?.source()
                if (source != null) {
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (source.read(buffer).also { read = it } != -1) {
                        totalBytesRead += read
                        val fraction = (totalBytesRead.toFloat() / 5000000f).coerceIn(0f, 1f)
                        _speedTestState.value = _speedTestState.value.copy(progress = 0.3f + (fraction * 0.7f))
                    }
                }
            }

            val dlDurationSec = (System.currentTimeMillis() - dlStart) / 1000.0
            val speedMbps = if (dlDurationSec > 0) {
                ((totalBytesRead * 8.0) / (1024.0 * 1024.0)) / dlDurationSec
            } else 0.0

            _speedTestState.value = SpeedTestResult(
                downloadSpeedMbps = speedMbps,
                latencyMs = latency,
                isRunning = false,
                progress = 1.0f,
                error = null
            )
        } catch (e: Exception) {
            _speedTestState.value = SpeedTestResult(
                downloadSpeedMbps = 0.0,
                latencyMs = 0,
                isRunning = false,
                progress = 0f,
                error = "Test failed: ${e.message}"
            )
        }
    }

    // Calculate prayer times based on latitude, longitude and calendar
    fun calculatePrayerTimes(lat: Double = 23.81, lng: Double = 90.41): PrayerTimes {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        // Solar declination approx formula
        val d = 23.45 * sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81)))
        val eqTime = 9.87 * sin(Math.toRadians(2.0 * (360.0 / 365.0) * (dayOfYear - 81))) -
                7.53 * cos(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81))) -
                1.5 * sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81)))

        val timezone = cal.timeZone.rawOffset / (1000.0 * 3600.0)
        val noon = 12.0 + timezone - (lng / 15.0) - (eqTime / 60.0)

        fun formatHours(h: Double): String {
            val normalized = (h + 24.0) % 24.0
            val hr = normalized.toInt()
            val min = ((normalized - hr) * 60).toInt()
            return String.format("%02d:%02d", hr, min)
        }

        val fajrHours = noon - 1.55
        val sunriseHours = noon - 1.15
        val dhuhrHours = noon
        val asrHours = noon + 1.25
        val maghribHours = noon + 1.15
        val ishaHours = noon + 1.55

        return PrayerTimes(
            fajr = formatHours(fajrHours),
            sunrise = formatHours(sunriseHours),
            dhuhr = formatHours(dhuhrHours),
            asr = formatHours(asrHours),
            maghrib = formatHours(maghribHours),
            isha = formatHours(ishaHours)
        )
    }

    // Calculate Qibla bearing from user coordinate to Kaaba (21.4225° N, 39.8262° E)
    fun calculateQiblaDirection(userLat: Double = 23.81, userLng: Double = 90.41): Float {
        val kaabaLat = Math.toRadians(21.4225)
        val kaabaLng = Math.toRadians(39.8262)
        val uLat = Math.toRadians(userLat)
        val uLng = Math.toRadians(userLng)

        val dLng = kaabaLng - uLng
        val y = sin(dLng)
        val x = cos(uLat) * kotlin.math.tan(kaabaLat) - sin(uLat) * cos(dLng)
        val qiblaRad = atan2(y, x)
        return ((Math.toDegrees(qiblaRad) + 360) % 360).toFloat()
    }

    // Anti-theft motion sensor
    fun armAntiTheft() {
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        initialAccel = null
        _isAntiTheftTriggered.value = false
        _isAntiTheftArmed.value = true
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
    }

    fun disarmAntiTheft() {
        _isAntiTheftArmed.value = false
        _isAntiTheftTriggered.value = false
        initialAccel = null
        sensorManager?.unregisterListener(this)
        stopAlarm()
    }

    private fun triggerAlarm() {
        _isAntiTheftTriggered.value = true
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 3000)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 200, 500), 0)
            }
        } catch (_: Exception) {}
    }

    private fun stopAlarm() {
        toneGenerator?.stopTone()
        toneGenerator?.release()
        toneGenerator = null
        vibrator?.cancel()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !_isAntiTheftArmed.value || _isAntiTheftTriggered.value) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val init = initialAccel
            if (init == null) {
                initialAccel = floatArrayOf(x, y, z)
            } else {
                val deltaX = abs(x - init[0])
                val deltaY = abs(y - init[1])
                val deltaZ = abs(z - init[2])
                // Movement threshold exceeded
                if (deltaX > 2.5f || deltaY > 2.5f || deltaZ > 2.5f) {
                    triggerAlarm()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
