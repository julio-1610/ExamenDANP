package com.example.examen

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.example.examen.components.RoomScreen
import com.example.examen.tools.BeaconScanListener
import com.example.examen.tools.BeaconScanner
import com.example.examen.tools.trilateration
import com.example.examen.ui.theme.ExamenTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity(), BeaconScanListener {
    private lateinit var beaconScanner: BeaconScanner
    private val viewModel: MainViewModel by viewModels()

    private val beaconList = LinkedHashMap<String, Beacon>()
    val uuid1 = "3ab11a6e-867d-48d5-828d-67f16cced0ca"
    val uuid2 = "00000000-0000-1000-8000-00805f9b34fb"
    val uuid3 = "00000000-0000-1000-8000-00805f9b34fa"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize BeaconScanner
        beaconScanner = BeaconScanner(this, this)
        beaconList[uuid1] =
            Beacon(uuid1, "1", "1", 0.0, Cardinal(8.0, 5.0))
        beaconList[uuid2] =
            Beacon(uuid2, "1", "2", 0.0, Cardinal(8.0, 13.0))
        beaconList[uuid3] =
            Beacon(uuid3, "1", "2", 0.0, Cardinal(15.0, 8.0))

        beaconScanner.startScanning()

        setContent {
            ExamenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "- AULA 202",
                        modifier = Modifier.padding(innerPadding)
                    )
                    RoomScreen(viewModel)
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
            "Beacons detected with UUID 1: ${beaconList.get(uuid1)?.minor}, Distance: ${
                beaconList.get(uuid1)?.distance
            } meters" +
                    "Beacons detected with UUID 1: ${beaconList.get(uuid2)?.minor}, Distance: ${
                        beaconList.get(
                            uuid2
                        )?.distance
                    } meters" +
                    "Beacons detected with UUID 1: ${beaconList.get(uuid3)?.minor}, Distance: ${
                        beaconList.get(
                            uuid3
                        )?.distance
                    } meters"

        )
        val result = trilateration(
            beaconList.get(uuid1)!!.cardinal,
            beaconList.get(uuid2)!!.cardinal,
            beaconList.get(uuid3)!!.cardinal,
            beaconList.get(uuid1)!!.distance,
            beaconList.get(uuid2)!!.distance,
            beaconList.get(uuid3)!!.distance
        )

        viewModel.updateResult(Offset(result.x.toFloat(), result.y.toFloat()))

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

class MainViewModel : ViewModel() {
    private val _result = MutableStateFlow(Offset(50f, 50f))
    val result: StateFlow<Offset> get() = _result

    fun updateResult(newResult: Offset) {
        _result.value = newResult
    }
}