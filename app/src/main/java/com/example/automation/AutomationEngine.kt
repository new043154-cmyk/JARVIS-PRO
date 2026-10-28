package com.example.automation

import android.content.Context
import com.example.device.DeviceActionManager
import com.example.model.ActionType
import com.example.model.AutomationRule
import com.example.model.TriggerType
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AutomationEngine(
    context: Context,
    private val deviceActionManager: DeviceActionManager,
    private val voiceManager: VoiceManager,
    private val onRunDailyBriefing: () -> Unit
) {
    private val prefs = context.getSharedPreferences("jarvis_automations_prefs", Context.MODE_PRIVATE)

    private val _rules = MutableStateFlow<List<AutomationRule>>(emptyList())
    val rules: StateFlow<List<AutomationRule>> = _rules.asStateFlow()

    init {
        loadRules()
    }

    private fun loadRules() {
        val jsonStr = prefs.getString("saved_rules", null)
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<AutomationRule>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        AutomationRule(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            triggerType = TriggerType.valueOf(obj.getString("triggerType")),
                            triggerParam = obj.optString("triggerParam", ""),
                            actionType = ActionType.valueOf(obj.getString("actionType")),
                            actionParam = obj.optString("actionParam", ""),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            lastExecuted = obj.optLong("lastExecuted", 0L)
                        )
                    )
                }
                _rules.value = list
            } catch (_: Exception) {
                initDefaultRules()
            }
        } else {
            initDefaultRules()
        }
    }

    private fun initDefaultRules() {
        val defaults = listOf(
            AutomationRule(
                title = "Morning Stark Briefing",
                triggerType = TriggerType.SCHEDULED_TIME,
                triggerParam = "08:00",
                actionType = ActionType.DAILY_BRIEFING,
                actionParam = "",
                isEnabled = true
            ),
            AutomationRule(
                title = "Critical Power Alert",
                triggerType = TriggerType.BATTERY_LOW,
                triggerParam = "20",
                actionType = ActionType.SPEAK_ANNOUNCEMENT,
                actionParam = "Warning Sir: Battery reserves have fallen below twenty percent. Connecting auxiliary power recommended.",
                isEnabled = true
            ),
            AutomationRule(
                title = "Arc Reactor Charge Connected",
                triggerType = TriggerType.CHARGER_CONNECTED,
                triggerParam = "",
                actionType = ActionType.SPEAK_ANNOUNCEMENT,
                actionParam = "External power source engaged. Charging Stark systems.",
                isEnabled = true
            ),
            AutomationRule(
                title = "Night Torch Protocol",
                triggerType = TriggerType.VOICE_SHORTCUT,
                triggerParam = "lumos",
                actionType = ActionType.TOGGLE_FLASHLIGHT,
                actionParam = "",
                isEnabled = true
            )
        )
        _rules.value = defaults
        saveRulesToPrefs(defaults)
    }

    fun addRule(rule: AutomationRule) {
        val updated = _rules.value + rule
        _rules.value = updated
        saveRulesToPrefs(updated)
    }

    fun toggleRule(id: String) {
        val updated = _rules.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
        _rules.value = updated
        saveRulesToPrefs(updated)
    }

    fun deleteRule(id: String) {
        val updated = _rules.value.filter { it.id != id }
        _rules.value = updated
        saveRulesToPrefs(updated)
    }

    fun executeRule(rule: AutomationRule): String {
        when (rule.actionType) {
            ActionType.SPEAK_ANNOUNCEMENT -> {
                voiceManager.speak(rule.actionParam.ifEmpty { "Automation triggered: ${rule.title}" })
            }
            ActionType.TOGGLE_FLASHLIGHT -> {
                deviceActionManager.toggleFlashlight()
            }
            ActionType.DAILY_BRIEFING -> {
                onRunDailyBriefing()
            }
            ActionType.OPEN_APP -> {
                if (rule.actionParam.isNotEmpty()) {
                    deviceActionManager.launchAppByName(rule.actionParam)
                }
            }
            ActionType.VIBRATE_ALERT -> {
                voiceManager.speak("Alert activated")
            }
        }

        // Update last executed
        val updated = _rules.value.map {
            if (it.id == rule.id) it.copy(lastExecuted = System.currentTimeMillis()) else it
        }
        _rules.value = updated
        saveRulesToPrefs(updated)
        return "Executed: ${rule.title}"
    }

    private fun saveRulesToPrefs(list: List<AutomationRule>) {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
                .put("id", item.id)
                .put("title", item.title)
                .put("triggerType", item.triggerType.name)
                .put("triggerParam", item.triggerParam)
                .put("actionType", item.actionType.name)
                .put("actionParam", item.actionParam)
                .put("isEnabled", item.isEnabled)
                .put("lastExecuted", item.lastExecuted)
            array.put(obj)
        }
        prefs.edit().putString("saved_rules", array.toString()).apply()
    }
}
