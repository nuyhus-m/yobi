package dev.onesoftdigm.fitrus.fitrusestdevicesdkdemo

import android.app.ProgressDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup
import com.onesoftdigm.fitrus.device.sdk.FitrusBleDelegate
import com.onesoftdigm.fitrus.device.sdk.FitrusDevice
import com.onesoftdigm.fitrus.device.sdk.Gender
import dev.onesoftdigm.fitrus.fitrusestdevicesdkdemo.R
import kotlin.math.round

class MainActivity : FitrusBleDelegate, AppCompatActivity() {
    private lateinit var txtBirth: EditText
    private lateinit var txtGender: EditText
    private lateinit var txtHeight: EditText
    private lateinit var txtWeight: EditText
    private lateinit var txtCorrect: EditText
    private lateinit var txtBaseSystolic: EditText
    private lateinit var txtBaseDiastolic: EditText
    private lateinit var segmentedButtonGroup: SegmentedButtonGroup
    private lateinit var lblConnected: TextView
    private lateinit var btnStartScan: Button
    private lateinit var btnStartMeasure: Button
    private lateinit var btnDisconnectDevice: Button
    private lateinit var txtResult: TextView
    private lateinit var manager: FitrusDevice
    private var type: String = "comp"
    private var measuring: Boolean = false
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialog = ProgressDialog(this)
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        manager = FitrusDevice(this, this, "normal_key")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        txtBirth = findViewById(R.id.txtBirth)
        txtGender = findViewById(R.id.txtGender)
        txtHeight = findViewById(R.id.txtHeight)
        txtWeight = findViewById(R.id.txtWeight)
        txtCorrect = findViewById(R.id.txtCorrect)
        txtBaseSystolic = findViewById(R.id.txtBaseSystolic)
        txtBaseDiastolic = findViewById(R.id.txtBaseDiastolic)
        segmentedButtonGroup = findViewById(R.id.segmentedButtonGroup)
        lblConnected = findViewById(R.id.lblConnected)
        btnStartScan = findViewById(R.id.btnStartScan)
        btnStartMeasure = findViewById(R.id.btnStartMeasure)
        btnDisconnectDevice = findViewById(R.id.btnDisconnectDevice)
        txtResult = findViewById(R.id.txtResult)

        segmentedButtonGroup.setOnPositionChangedListener {
            when (it) {
                0 -> type = "comp"
                1 -> type = "device"
                2 -> type = "battery"
                3 -> type = "heart"
                4 -> type = "bp"
                5 -> type = "stress"
                6 -> type = "tempBody"
                7 -> type = "tempObj"
            }
            btnStartMeasure.text = "Start"
        }

        btnDisconnectDevice.setOnClickListener {
            Log.d(TAG, "disconnect button click")
            if (manager.fitrusConnectionState)
                manager.disconnectFitrus()
            else {
                if (manager.fitrusScanState) {
                    manager.stopFitrusScan()
                    lblConnected.text = "not connected"
                } else {
                    Log.d(TAG, "이미 연결 종료")
                }
            }
        }

        btnStartScan.setOnClickListener {
            Log.d(TAG, "btnScanStart click!!!")
            if (manager.fitrusConnectionState) {
                Log.d(TAG, "이미 연결되어 있습니다.")
            } else {
                if (manager.fitrusScanState) {
                    manager.startFitrusScan()
                    lblConnected.text = "scanning"
                } else {
                    showAlert("warning", "블루투스 사용 불가")
                }
            }
        }

        btnStartMeasure.setOnClickListener {
            if (measuring) return@setOnClickListener
            txtResult.text = ""
            Log.d(TAG, "측정 시작")
            measuring = true

            if (txtBirth.text.isEmpty() || txtGender.text.isEmpty() || txtHeight.text.isEmpty() || txtWeight.text.isEmpty()) {
                showAlert("Warning", "사용자 정보를 입력하세요")
                return@setOnClickListener
            }

            if (type in listOf("device", "battery", "tempObj"))
                measureStart()
            else {
                if (manager.fitrusConnectionState) {
                    object : CountDownTimer(5000, 1000) {
                        override fun onTick(p0: Long) {
                            lblConnected.text = "${p0 / 1000}"
                        }

                        override fun onFinish() {
                            measureStart()
                        }
                    }.start()
                } else {
                    Log.d(TAG, "disconnected during timer pressing")
                    lblConnected.text = "not connected"
                }
            }
        }
    }

    fun showAlert(title: String, body: String) {
        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(body)
        alertDialog.show()
    }

    override fun handleFitrusConnected() {
        lblConnected.text = "connected"
        btnStartMeasure.isVisible = true
        btnStartMeasure.isEnabled = true
    }

    override fun handleFitrusDisconnected() {
        lblConnected.text = "not connected"
        btnStartMeasure.isVisible = false
        btnStartMeasure.isEnabled = false
        measuring = false
    }

    override fun handleFitrusDeviceInfo(result: Map<String, String>) {
        txtResult.text = result.map { "${it.key} : ${it.value}" }.joinToString("\n")
        measuring = false
    }

    override fun handleFitrusBatteryInfo(result: Map<String, Any>) {
        txtResult.text = result.map { "${it.key} : ${it.value}" }.joinToString("\n")
        measuring = false
    }

    override fun handleFitrusCompMeasured(result: Map<String, String>) {
        if (dialog.isShowing) dialog.dismiss()

        txtResult.text = result.map { "${it.key} : ${it.value}" }.joinToString("\n")
        measuring = false
        manager.disconnectFitrus()
    }

    override fun handleFitrusPpgMeasured(result: Map<String, Any>) {
        if (dialog.isShowing) dialog.dismiss()

        txtResult.text = result.map { "${it.key} : ${it.value}" }.joinToString("\n")
        measuring = false
        manager.disconnectFitrus()
    }

    override fun handleFitrusTempMeasured(result: Map<String, String>) {
        if (dialog.isShowing) dialog.dismiss()
        txtResult.text = result.map { "${it.key} : ${it.value}" }.joinToString("\n")
        manager.disconnectFitrus()
    }

    override fun fitrusDispatchError(error: String) {
        if (dialog.isShowing) dialog.dismiss()
        showAlert("Error", error)
    }

    companion object {
        const val TAG = "Fitrus Main Activity"
    }

    fun measureStart() {
        when (type) {
            "comp" -> {
                dialog.show()
                manager.startFitrusCompMeasure(
                    Gender.valueOf(txtGender.text.toString().uppercase()),
                    round(txtHeight.text.toString().toFloat() * 100) / 100,
                    round(txtWeight.text.toString().toFloat() * 100) / 100,
                    txtBirth.text.toString(),
                    round(txtCorrect.text.toString().toFloat() * 100) / 100,
                )
            }
            "device" -> manager.getDeviceInfoAll()
            "battery" -> manager.getBatteryInfo()
            "heart" -> {
                dialog.show()
                manager.startFitrusHeartRateMeasure()
            }
            "bp" -> {
                dialog.show()
                manager.StartFitrusBloodPressure(txtBaseSystolic.text.toString().toFloat(), txtBaseDiastolic.text.toString().toFloat())
            }
            "stress" -> {
                dialog.show()
                manager.startFitrusStressMeasure(txtBirth.text.toString())
            }
            "tempBody" -> manager.startFitrusTempBodyMeasure()
            "tempObj" -> manager.startFitrusTempObjectMeasure()
        }
    }
}