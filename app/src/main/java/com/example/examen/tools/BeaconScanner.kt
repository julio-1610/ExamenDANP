package com.example.examen.tools

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BeaconScanner(private val activity: Activity) {

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device: BluetoothDevice = result.device
            val scanRecord = result.scanRecord
            val uuid = scanRecord?.serviceUuids?.get(0)?.uuid
            val rssi = result.rssi
            val txPower = -59 // Typical TX Power value for BLE beacons in dBm. Adjust as needed.

            val distance = calculateDistance(rssi, txPower)

            activity.runOnUiThread {
                Toast.makeText(activity, "Beacon detected with UUID: $uuid, Distance: $distance meters", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (result in results) {
                val device: BluetoothDevice = result.device
                val scanRecord = result.scanRecord
                val uuid = scanRecord?.serviceUuids?.get(0)?.uuid
                val rssi = result.rssi
                val txPower = -59 // Typical TX Power value for BLE beacons in dBm. Adjust as needed.

                val distance = calculateDistance(rssi, txPower)

                activity.runOnUiThread {
                    Toast.makeText(activity, "Beacon detected with UUID: $uuid, Distance: $distance meters", Toast.LENGTH_SHORT).show()
                }
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
            // Stop scanning after a pre-defined scan period
            handler.postDelayed({
                bluetoothLeScanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)

            val scanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString("YOUR-BEACON-UUID-HERE")) // Replace with your beacon UUID
                .build()
            val scanFilters = listOf(scanFilter)

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothLeScanner?.startScan(scanFilters, scanSettings, leScanCallback)
        } catch (e: SecurityException) {
            Toast.makeText(activity, "Permission denied for Bluetooth scanning", Toast.LENGTH_SHORT).show()
            Log.e("BeaconScanner", "SecurityException: ${e.message}")
        }
    }

    fun stopScanning() {
        try {
            bluetoothLeScanner?.stopScan(leScanCallback)
        } catch (e: SecurityException) {
            Toast.makeText(activity, "Permission denied for stopping Bluetooth scanning", Toast.LENGTH_SHORT).show()
            Log.e("BeaconScanner", "SecurityException: ${e.message}")
        }
    }

    // Method to calculate distance based on RSSI and TxPower
    private fun calculateDistance(rssi: Int, txPower: Int): Double {
        if (rssi == 0) {
            return -1.0 // if we cannot determine distance, return -1.
        }

        val ratio = rssi * 1.0 / txPower
        return if (ratio < 1.0) {
            Math.pow(ratio, 10.0)
        } else {
            0.89976 * Math.pow(ratio, 7.7095) + 0.111
        }
    }
}
