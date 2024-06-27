package com.example.examen.tools

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

interface BeaconScanListener {
    fun onBeaconDetected(uuid: String, distance: Double)
}

class BeaconScanner(private val activity: Activity, private val listener: BeaconScanListener) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val uuids = listOf(
        "3ab11a6e-867d-48d5-828d-67f16cced0ca",
        "00000000-0000-1000-8000-00805f9b34fb",  // Ejemplo de segundo UUID
        "00000000-0000-1000-8000-00805f9b34fa"
    )

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            processScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (result in results) {
                processScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            activity.runOnUiThread {
                Toast.makeText(activity, "Scan failed with error: $errorCode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startScanning() {
        // Check permissions
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        try {
            val scanFilters = uuids.map {
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString(it))
                    .build()
            }

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
        } catch (e: SecurityException) {
            Log.e("BeaconScanner", "SecurityException: ${e.message}")
        }
    }

    fun stopScanning() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.e("BeaconScanner", "SecurityException: ${e.message}")
        }
    }

    private fun processScanResult(result: ScanResult) {
        val scanRecord = result.scanRecord ?: return
        val uuid = scanRecord.serviceUuids?.get(0)?.uuid?.toString() ?: return
        val rssi = result.rssi
        val txPower = -59 // Typical TX Power value for BLE beacons in dBm. Adjust as needed.
        val distance = calculateDistance(rssi, txPower)

        activity.runOnUiThread {
            listener.onBeaconDetected(uuid, distance)
        }
    }

    // Method to calculate distance based on RSSI and TxPower
    private fun calculateDistance(rssi: Int, txPower: Int): Double {
        val kalmanFilter = KalmanFilter(Q = 0.1, R = 0.1)

        if (rssi == 0) {
            return -1.0 // if we cannot determine distance, return -1.
        }

        val ratio = rssi * 1.0 / txPower
        val distance = if (ratio < 1.0) {
            Math.pow(ratio, 10.0)
        } else {
            0.89976 * Math.pow(ratio, 7.7095) + 0.111
        }

        return kalmanFilter.filter(distance)
    }

    private class KalmanFilter(
        private var Q: Double, // Process noise covariance
        private var R: Double, // Measurement noise covariance
        private var A: Double = 1.0, // State transition coefficient
        private var B: Double = 0.0, // Control input coefficient
        private var H: Double = 1.0 // Measurement coefficient
    ) {
        private var x: Double = 0.0 // Initial estimate
        private var P: Double = 1.0 // Initial estimate covariance

        fun filter(z: Double, u: Double = 0.0): Double {
            // Prediction
            val x_pred = A * x + B * u
            val P_pred = A * P * A + Q

            // Update
            val K = P_pred * H / (H * P_pred * H + R) // Kalman gain
            x = x_pred + K * (z - H * x_pred)
            P = (1 - K * H) * P_pred

            return x
        }
    }

}
