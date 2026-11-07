package gr.archerylab.score

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Collections

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private var immersive = false

    private fun applyImmersive() {
        WindowCompat.setDecorFitsSystemWindows(window, !immersive)
        val c = WindowInsetsControllerCompat(window, window.decorView)
        if (immersive) {
            c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            c.hide(WindowInsetsCompat.Type.systemBars())
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            c.show(WindowInsetsCompat.Type.systemBars())
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && immersive) applyImmersive()
    }
    private val scanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val t = r.data?.getStringExtra("text") ?: ""
        val e = r.data?.getStringExtra("err") ?: ""
        if (t.isNotEmpty()) sendScan(t) else if (e.isNotEmpty())
            web.evaluateJavascript("window.onNativeScanError&&window.onNativeScanError(" + JSONObject.quote(e) + ")", null)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.statusBarColor = Color.parseColor("#0A0D11")
        window.navigationBarColor = Color.parseColor("#0A0D11")
        web = WebView(this)
        web.setBackgroundColor(Color.parseColor("#0A0D11"))
        setContentView(web)
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this)).build()
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                loader.shouldInterceptRequest(request.url)
        }
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.addJavascriptInterface(Bridge(), "Native")
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    private fun sendScan(text: String) {
        web.evaluateJavascript("window.onNativeScan(" + JSONObject.quote(text) + ")", null)
    }

    inner class Bridge {
        @JavascriptInterface
        fun scan() {
            runOnUiThread { scanLauncher.launch(Intent(this@MainActivity, ScanActivity::class.java)) }
        }

        @JavascriptInterface
        fun ip(): String {
            try {
                val all = Collections.list(NetworkInterface.getNetworkInterfaces()).filter { it.isUp && !it.isLoopback }
                val pref = all.sortedBy { if (Regex("wlan|ap\\d|swlan").containsMatchIn(it.name)) 0 else 1 }
                for (i in pref) for (a in Collections.list(i.inetAddresses))
                    if (a is Inet4Address && !a.isLoopbackAddress) return a.hostAddress ?: ""
            } catch (e: Exception) { }
            return ""
        }

        @JavascriptInterface
        fun version(): String = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
        } catch (e: Exception) { "?" }

        @JavascriptInterface
        fun fullscreen(on: Boolean) {
            runOnUiThread {
                immersive = on
                applyImmersive()
            }
        }

        @JavascriptInterface
        fun orient(mode: String) {
            runOnUiThread {
                requestedOrientation = if (mode == "portrait") ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }
}
