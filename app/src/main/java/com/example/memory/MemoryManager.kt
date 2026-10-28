package com.example.memory

import android.content.Context
import com.example.model.ExpenseItem
import com.example.model.MemoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class MemoryManager(context: Context) {
    private val prefs = context.getSharedPreferences("jarvis_memories_prefs", Context.MODE_PRIVATE)

    private val _memories = MutableStateFlow<List<MemoryEntry>>(emptyList())
    val memories: StateFlow<List<MemoryEntry>> = _memories.asStateFlow()

    private val _expenses = MutableStateFlow<List<ExpenseItem>>(emptyList())
    val expenses: StateFlow<List<ExpenseItem>> = _expenses.asStateFlow()

    init {
        loadMemories()
        loadExpenses()
    }

    private fun loadMemories() {
        val jsonStr = prefs.getString("saved_memories", null)
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<MemoryEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        MemoryEntry(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            category = obj.optString("category", "General"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _memories.value = list
            } catch (_: Exception) {
                initDefaultMemories()
            }
        } else {
            initDefaultMemories()
        }
    }

    private fun initDefaultMemories() {
        val defaults = listOf(
            MemoryEntry(title = "Primary User", content = "User is addressed as Sir / Boss", category = "Preference"),
            MemoryEntry(title = "Preferred Assistant", content = "J.A.R.V.I.S. for analytical tasks, P.A.R.I. for conversations", category = "Preference"),
            MemoryEntry(title = "Mission Statement", content = "Autonomous, high-reliability personal AI system for Android", category = "Fact")
        )
        _memories.value = defaults
        saveMemoriesToPrefs(defaults)
    }

    fun addMemory(title: String, content: String, category: String = "General") {
        val entry = MemoryEntry(title = title, content = content, category = category)
        val updated = _memories.value + entry
        _memories.value = updated
        saveMemoriesToPrefs(updated)
    }

    fun deleteMemory(id: String) {
        val updated = _memories.value.filter { it.id != id }
        _memories.value = updated
        saveMemoriesToPrefs(updated)
    }

    fun clearAllMemories() {
        _memories.value = emptyList()
        saveMemoriesToPrefs(emptyList())
    }

    private fun saveMemoriesToPrefs(list: List<MemoryEntry>) {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
                .put("id", item.id)
                .put("title", item.title)
                .put("content", item.content)
                .put("category", item.category)
                .put("timestamp", item.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("saved_memories", array.toString()).apply()
    }

    // Expenses management
    private fun loadExpenses() {
        val jsonStr = prefs.getString("saved_expenses", null)
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<ExpenseItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ExpenseItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _expenses.value = list
            } catch (_: Exception) {}
        }
    }

    fun addExpense(title: String, amount: Double, category: String) {
        val item = ExpenseItem(title = title, amount = amount, category = category)
        val updated = _expenses.value + item
        _expenses.value = updated
        saveExpensesToPrefs(updated)
    }

    fun deleteExpense(id: String) {
        val updated = _expenses.value.filter { it.id != id }
        _expenses.value = updated
        saveExpensesToPrefs(updated)
    }

    private fun saveExpensesToPrefs(list: List<ExpenseItem>) {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
                .put("id", item.id)
                .put("title", item.title)
                .put("amount", item.amount)
                .put("category", item.category)
                .put("timestamp", item.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("saved_expenses", array.toString()).apply()
    }
}
