package com.example.coding

import com.example.model.CodingFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CodingProjectManager {

    private val sampleJava = """package com.jarvis.demo;

public class JarvisAssistant {
    private String name = "JARVIS PRO";
    private boolean isOnline = true;

    public void initializeCore() {
        System.out.println("JARVIS Core v1.0.0 Online.");
        System.out.println("Arc Reactor Power: 100%");
    }

    public String processCommand(String command) {
        if (command.equalsIgnoreCase("status")) {
            return "All Stark Industries subroutines operational.";
        }
        return "Command received: " + command;
    }
}
""".trimIndent()

    private val sampleHtml = """<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>JARVIS HUD Canvas</title>
    <style>
        body {
            margin: 0;
            background: #080c14;
            color: #00e5ff;
            font-family: 'Courier New', monospace;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100vh;
        }
        .reactor {
            width: 140px;
            height: 140px;
            border: 3px solid #00e5ff;
            border-radius: 50%;
            box-shadow: 0 0 25px #00e5ff, inset 0 0 25px #00e5ff;
            animation: pulse 2s infinite ease-in-out;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .core {
            width: 70px;
            height: 70px;
            background: radial-gradient(circle, #ffffff, #00e5ff);
            border-radius: 50%;
            box-shadow: 0 0 35px #00e5ff;
        }
        @keyframes pulse {
            0%, 100% { transform: scale(0.95); opacity: 0.8; }
            50% { transform: scale(1.05); opacity: 1; }
        }
        h2 { margin-top: 20px; letter-spacing: 3px; }
        p { color: #80deea; font-size: 13px; }
    </style>
</head>
<body>
    <div class="reactor"><div class="core"></div></div>
    <h2>JARVIS PRO</h2>
    <p>HOLOGRAPHIC INTERFACE ONLINE</p>
</body>
</html>
""".trimIndent()

    private val samplePython = """# Stark Protocol Diagnostics
import time

def scan_perimeters():
    systems = ["Power Core", "HUD Optics", "Aero Thrusters", "Shields"]
    for sys in systems:
        print(f"Checking {sys}... [OK]")
        time.sleep(0.1)
    print("All subsystems green.")

if __name__ == "__main__":
    scan_perimeters()
""".trimIndent()

    private val _files = MutableStateFlow(
        listOf(
            CodingFile(name = "JarvisAssistant.java", language = "java", content = sampleJava),
            CodingFile(name = "index.html", language = "html", content = sampleHtml),
            CodingFile(name = "diagnostics.py", language = "python", content = samplePython)
        )
    )
    val files: StateFlow<List<CodingFile>> = _files.asStateFlow()

    private val _activeFileIndex = MutableStateFlow(0)
    val activeFileIndex: StateFlow<Int> = _activeFileIndex.asStateFlow()

    fun getActiveFile(): CodingFile? {
        val list = _files.value
        val index = _activeFileIndex.value
        return if (index in list.indices) list[index] else null
    }

    fun selectFile(index: Int) {
        if (index in _files.value.indices) {
            _activeFileIndex.value = index
        }
    }

    fun updateActiveContent(newContent: String) {
        val index = _activeFileIndex.value
        val list = _files.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(content = newContent, isModified = true)
            _files.value = list
        }
    }

    fun createFile(name: String, language: String, initialContent: String = "") {
        val newFile = CodingFile(name = name, language = language, content = initialContent)
        val list = _files.value.toMutableList()
        list.add(newFile)
        _files.value = list
        _activeFileIndex.value = list.size - 1
    }

    fun deleteFile(index: Int) {
        val list = _files.value.toMutableList()
        if (list.size > 1 && index in list.indices) {
            list.removeAt(index)
            _files.value = list
            _activeFileIndex.value = 0
        }
    }

    fun loadTemplate(templateName: String) {
        when (templateName) {
            "Android Java Activity" -> {
                val content = """package com.example.myapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Hello Stark Industries!");
        setContentView(tv);
    }
}
""".trimIndent()
                createFile("MainActivity.java", "java", content)
            }
            "HTML5 Canvas Game" -> {
                val content = """<!DOCTYPE html>
<html>
<body style="margin:0;background:#111;overflow:hidden;display:flex;justify-content:center;align-items:center;height:100vh;">
<canvas id="c" width="320" height="400" style="border:1px solid #00e5ff;"></canvas>
<script>
const c=document.getElementById('c'), ctx=c.getContext('2d');
let y=200, dy=2;
function draw(){
  ctx.fillStyle='#0a0e1a';ctx.fillRect(0,0,320,400);
  ctx.fillStyle='#00e5ff';ctx.beginPath();ctx.arc(160,y,20,0,Math.PI*2);ctx.fill();
  y+=dy; if(y>380||y<20)dy*=-1;
  requestAnimationFrame(draw);
}
draw();
</script>
</body>
</html>
""".trimIndent()
                createFile("game.html", "html", content)
            }
            "REST API Client" -> {
                val content = """import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiClient {
    private final OkHttpClient client = new OkHttpClient();

    public String fetchWeather() throws Exception {
        Request request = new Request.Builder()
            .url("https://api.open-meteo.com/v1/forecast?latitude=23.81&longitude=90.41&current_weather=true")
            .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
""".trimIndent()
                createFile("ApiClient.java", "java", content)
            }
        }
    }
}
