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
        "00001815-0000-1000-8000-00805f9b34fa",
        "00001815-0000-1000-8000-00805f9b34fb",
        "00001815-0000-1000-8000-00805f9b34fc"
    )

    // Mapa para almacenar instancias de KalmanFilter por UUID
    private val kalmanFilters = mutableMapOf<String, KalmanFilter>()

    // Mapa para almacenar TX Power por UUID
    private val txPowerMap = mapOf(
        "00001815-0000-1000-8000-00805f9b34fa" to -59,  // Ajustar según el beacon
        "00001815-0000-1000-8000-00805f9b34fb" to -59,
        "00001815-0000-1000-8000-00805f9b34fc" to -59
    )

    // Mapa para almacenar buffers de RSSI por UUID (para filtro de media móvil)
    private val rssiBuffers = mutableMapOf<String, MutableList<Int>>()

    // Tamaño del buffer para el filtro de media móvil
    private val bufferSize = 10

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

        // Aplicar filtro de media móvil
        val filteredRssi = applyMovingAverageFilter(uuid, rssi)

        // Obtener TX Power para este UUID
        val txPower = txPowerMap[uuid] ?: return

        // Obtener filtro de Kalman para este UUID (crear si no existe)
        val kalmanFilter = kalmanFilters.getOrPut(uuid) { KalmanFilter(Q = 0.01, R = 0.1, C = 1.0) }

        // Calcular distancia con RSSI filtrado
        val distance = calculateDistance(filteredRssi, txPower)

        // Aplicar filtro de Kalman a la distancia calculada
        val filteredDistance = kalmanFilter.filter(distance)

        activity.runOnUiThread {
            listener.onBeaconDetected(uuid, filteredDistance)
        }
    }

    // Método para calcular la distancia basada en RSSI y TxPower
    private fun calculateDistance(rssi: Int, txPower: Int): Double {
        if (rssi == 0) {
            return -1.0 // Valor de RSSI inválido
        }

        val ratio = rssi * 1.0 / txPower
        return if (ratio < 1.0) {
            Math.pow(ratio, 10.0)
        } else {
            0.89976 * Math.pow(ratio, 7.7095) + 0.111
        }
    }

    // Aplicar filtro de media móvil a RSSI por UUID
    private fun applyMovingAverageFilter(uuid: String, rssi: Int): Int {
        val rssiBuffer = rssiBuffers.getOrPut(uuid) { mutableListOf() }
        rssiBuffer.add(rssi)
        if (rssiBuffer.size > bufferSize) {
            rssiBuffer.removeAt(0)
        }
        val average = rssiBuffer.average()
        return average.toInt()
    }

    // Clase KalmanFilter (puedes usar tu implementación modificada)
    private class KalmanFilter(
        private var Q: Double,
        private var R: Double,
        private var A: Double = 1.0,
        private var B: Double = 0.0,
        private var C: Double = 1.0
    ) {
        private var x: Double? = null
        private var cov: Double = 0.0

        private fun square(x: Double) = x * x
        private fun predict(x: Double): Double = (A * x)
        private fun uncertainty(): Double = (square(A) * cov) + R

        fun filter(signal: Double): Double {
            val x = this.x

            if (x == null) {
                this.x = (1 / C) * signal
                cov = square(1 / C) * Q
            } else {
                val prediction = predict(x)
                val uncertainty = uncertainty()

                val k_gain = uncertainty * C * (1 / ((square(C) * uncertainty) + Q))

                this.x = prediction + k_gain * (signal - (C * prediction))
                cov = uncertainty - (k_gain * C * uncertainty)
            }
            return this.x!!
        }
    }
}
