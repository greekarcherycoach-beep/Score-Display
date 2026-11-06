package gr.archerylab.score

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {
    private lateinit var pv: PreviewView
    private val executor = Executors.newSingleThreadExecutor()
    private val scanner by lazy {
        BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build())
    }
    @Volatile private var done = false

    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) startCamera()
        else finishWith(null, "Δεν δόθηκε άδεια κάμερας. Πηγαίνετε Ρυθμίσεις Android → Εφαρμογές → Archery Lab Score → Δικαιώματα → Κάμερα → Να επιτρέπεται.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val match = ViewGroup.LayoutParams.MATCH_PARENT
        val wrap = ViewGroup.LayoutParams.WRAP_CONTENT
        val root = FrameLayout(this)
        root.setBackgroundColor(Color.BLACK)
        pv = PreviewView(this)
        root.addView(pv, FrameLayout.LayoutParams(match, match))
        val tv = TextView(this)
        tv.text = "Στρέψτε την κάμερα στο QR code"
        tv.setTextColor(Color.WHITE)
        tv.textSize = 18f
        tv.gravity = Gravity.CENTER
        tv.setPadding(24, 56, 24, 24)
        tv.setBackgroundColor(Color.argb(140, 0, 0, 0))
        root.addView(tv, FrameLayout.LayoutParams(match, wrap, Gravity.TOP))
        val btn = Button(this)
        btn.text = "✖ Ακύρωση"
        btn.setOnClickListener { finishWith(null, null) }
        val lp = FrameLayout.LayoutParams(match, wrap, Gravity.BOTTOM)
        lp.setMargins(32, 32, 32, 72)
        root.addView(btn, lp)
        setContentView(root)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else permLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider = future.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(pv.surfaceProvider)
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                analysis.setAnalyzer(executor) { proxy ->
                    try {
                        if (!done) {
                            val img = InputImage.fromBitmap(proxy.toBitmap(), proxy.imageInfo.rotationDegrees)
                            val res = Tasks.await(scanner.process(img))
                            val t = res.firstOrNull { !it.rawValue.isNullOrEmpty() }?.rawValue
                            if (t != null && !done) { done = true; runOnUiThread { finishWith(t, null) } }
                        }
                    } catch (e: Exception) {
                    } finally {
                        proxy.close()
                    }
                }
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (e: Exception) {
                finishWith(null, "Η κάμερα δεν ξεκίνησε: " + (e.message ?: e.javaClass.simpleName))
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun finishWith(text: String?, err: String?) {
        setResult(RESULT_OK, Intent().putExtra("text", text ?: "").putExtra("err", err ?: ""))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
        scanner.close()
    }
}
