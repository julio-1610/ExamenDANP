package com.example.examen

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.examen.components.RoomScreen
import com.example.examen.tools.BeaconScanListener
import com.example.examen.tools.BeaconScanner
import com.example.examen.tools.trilateration
import com.example.examen.ui.theme.ExamenTheme

class MainActivity : ComponentActivity(), BeaconScanListener {
    private lateinit var beaconScanner: BeaconScanner


    private val beaconList = LinkedHashMap<String, Beacon>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize BeaconScanner
        beaconScanner = BeaconScanner(this, this)
        beaconList["3ab11a6e-867d-48d5-828d-67f16cced0ca"] =
            Beacon("3ab11a6e-867d-48d5-828d-67f16cced0ca", "1", "1", 0.0, Cardinal(8.0, 5.0))
        beaconList["00000000-0000-1000-8000-00805f9b34fb"] =
            Beacon("00000000-0000-1000-8000-00805f9b34fb", "1", "2", 0.0, Cardinal(8.0, 13.0))
        beaconList["a617ec53-9247-48b2-9d74-97353a897e52"] =
            Beacon("a617ec53-9247-48b2-9d74-97353a897e52", "1", "2", 0.0, Cardinal(15.0, 8.0))

        beaconScanner.startScanning()

        setContent {
            ExamenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "- AULA 202",
                        modifier = Modifier.padding(innerPadding)
                    )
                    RoomScreen()
                }
            }
        }


    }

    // Permission request launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                beaconScanner.startScanning()
            } else {
                Toast.makeText(this, "Permission denied for Bluetooth scanning", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        beaconScanner.stopScanning()
    }

    override fun onBeaconDetected(uuid: String, distance: Double) {

        beaconList[uuid] = Beacon(uuid, "1", "2", distance, Cardinal(beaconList[uuid]!!.cardinal.x, beaconList[uuid]!!.cardinal.y))

        Log.d(
            "MainActivity",
            "Beacons detected with UUID 1: ${beaconList.get("3ab11a6e-867d-48d5-828d-67f16cced0ca")?.minor}, Distance: ${
                beaconList.get("3ab11a6e-867d-48d5-828d-67f16cced0ca")?.distance
            } meters" +
                    "Beacons detected with UUID 1: ${beaconList.get("00000000-0000-1000-8000-00805f9b34fb")?.minor}, Distance: ${
                        beaconList.get(
                            "00000000-0000-1000-8000-00805f9b34fb"
                        )?.distance
                    } meters" +
                    "Beacons detected with UUID 1: ${beaconList.get("a617ec53-9247-48b2-9d74-97353a897e52")?.minor}, Distance: ${
                        beaconList.get(
                            "a617ec53-9247-48b2-9d74-97353a897e52"
                        )?.distance
                    } meters"

        )
        val result = trilateration(
            beaconList.get("3ab11a6e-867d-48d5-828d-67f16cced0ca")!!.cardinal,
            beaconList.get("00000000-0000-1000-8000-00805f9b34fb")!!.cardinal,
            beaconList.get("a617ec53-9247-48b2-9d74-97353a897e52")!!.cardinal,
            beaconList.get("3ab11a6e-867d-48d5-828d-67f16cced0ca")!!.distance,
            beaconList.get("00000000-0000-1000-8000-00805f9b34fb")!!.distance,
            beaconList.get("a617ec53-9247-48b2-9d74-97353a897e52")!!.distance
        )

        Log.d("MainActivity", "Resultado trilateracion $result ")

    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "UNSA $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExamenTheme {
        Greeting("Android")
    }
}

data class Beacon(
    var uuid: String,
    var mayor: String,
    var minor: String,
    var distance: Double,
    var cardinal: Cardinal

)

data class Cardinal(
    var x: Double,
    var y: Double

)
