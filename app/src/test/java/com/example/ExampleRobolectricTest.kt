package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AIProviderManager
import com.example.coding.CodingProjectManager
import com.example.device.DeviceActionManager
import com.example.memory.MemoryManager
import com.example.model.PersonaType
import com.example.model.ThemeType
import com.example.terminal.TerminalEngine
import com.example.tools.PowerToolsManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameResource() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS PRO", appName)
    }

    @Test
    fun testDeviceActionManagerBatteryAndStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val deviceActionManager = DeviceActionManager(context)
        val battery = deviceActionManager.getBatteryStatus()
        assertNotNull(battery)
        assertTrue(battery.percentage in 0..100)

        val storage = deviceActionManager.getStorageInfo()
        assertNotNull(storage)
        assertTrue(storage.totalGb >= 0.0)
    }

    @Test
    fun testCodingProjectManagerFiles() {
        val manager = CodingProjectManager()
        val files = manager.files.value
        assertTrue(files.isNotEmpty())
        assertEquals("JarvisAssistant.java", files[0].name)

        manager.loadTemplate("HTML5 Canvas Game")
        val updatedFiles = manager.files.value
        assertTrue(updatedFiles.any { it.name == "game.html" })
    }

    @Test
    fun testPowerToolsAstronomicalCalculations() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val toolsManager = PowerToolsManager(context)

        val prayerTimes = toolsManager.calculatePrayerTimes(23.81, 90.41)
        assertNotNull(prayerTimes.fajr)
        assertNotNull(prayerTimes.maghrib)
        assertTrue(prayerTimes.fajr.contains(":"))

        val qibla = toolsManager.calculateQiblaDirection(23.81, 90.41)
        assertTrue(qibla in 0f..360f)
    }

    @Test
    fun testMemoryManagerVault() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val memoryManager = MemoryManager(context)

        val initialCount = memoryManager.memories.value.size
        memoryManager.addMemory("Test Memory", "Test Content", "UnitTest")
        val afterCount = memoryManager.memories.value.size
        assertEquals(initialCount + 1, afterCount)
    }

    @Test
    fun testTerminalEngineExecution() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val deviceActionManager = DeviceActionManager(context)
        val aiProviderManager = AIProviderManager(deviceActionManager)
        val codingManager = CodingProjectManager()
        val terminal = TerminalEngine(deviceActionManager, aiProviderManager, codingManager)

        terminal.executeCommand("status", PersonaType.JARVIS)
        val history = terminal.history.value
        assertTrue(history.any { it.text.contains("STARK SYSTEM TELEMETRY") })
    }
}
