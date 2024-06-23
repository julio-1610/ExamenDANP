package com.example.examen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.random.Random


@Composable
fun RoomScreen() {
    // Para obtener la densidad del dispositivo
    val density = LocalDensity.current
    // Estado para almacenar la posición del círculo
    var circlePosition by remember { mutableStateOf(Offset(50f, 50f)) }

    // Estado para almacenar el tamaño del rectángulo negro
    var rectSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp) // Ajusta el padding según sea necesario

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val paddingPx = with(density) { 16.dp.toPx() }


                    // Dibuja el aula
                    val aulaWidth = size.width - paddingPx * 2
                    val aulaHeight = size.height - paddingPx * 2
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(paddingPx, paddingPx),
                        size = Size(aulaWidth, aulaHeight),
                        style = Stroke(width = 5f)
                    )
                    // CIRCULO ALEATORIO
                    rectSize = Size(aulaWidth, aulaHeight)
                    drawCircle(
                        color = Color.Magenta,
                        radius = with(density) { 15.dp.toPx() },
                        center = circlePosition
                    )

                    // Dibuja la pizarra
                    val pizarraWidth = 300f
                    val pizarraHeight = 50f
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(size.width / 2 - pizarraWidth / 2, paddingPx),
                        size = Size(pizarraWidth, pizarraHeight),
                        style = Stroke(width = 5f)
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "PIZARRA",
                        size.width / 2,
                        paddingPx + pizarraHeight / 1.5f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 20f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )

                    // Dibuja el escritorio del profesor
                    val deskProfWidth = 100f
                    val deskProfHeight = 50f
                    drawCircle(
                        color = Color.Black,
                        radius = 12.5f,
                        center = Offset(size.width - deskProfWidth / 2 - paddingPx, paddingPx + pizarraHeight + + deskProfHeight + 12.5f)
                    )
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(size.width - deskProfWidth - paddingPx, paddingPx + pizarraHeight + paddingPx + deskProfHeight),
                        size = Size(deskProfWidth, deskProfHeight),
                        style = Stroke(width = 5f)
                    )
                    // Dibuja la puerta
                    val puertaWidth = 40f
                    val puertaHeight = 100f
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(paddingPx, paddingPx + pizarraHeight + paddingPx + deskProfHeight),
                        size = Size(puertaWidth, puertaHeight),
                        style = Stroke(width = 5f)
                    )

                    // Calcula las coordenadas para el texto "PUERTA"
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 20f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val textBounds = android.graphics.Rect()
                    textPaint.getTextBounds("PUERTA", 0, "PUERTA".length, textBounds)

                    // Coordenadas del centro del rectángulo de la puerta
                    val doorCenterX = paddingPx + puertaWidth / 2
                    val doorCenterY = paddingPx*2 + pizarraHeight + deskProfHeight + puertaHeight / 2

                    // Ajusta textY para centrar verticalmente el texto "PUERTA"
                    val textY = doorCenterY + textBounds.height() / 2  // Ajuste aquí para centrar

                    // Dibuja el texto "PUERTA" dentro del rectángulo de la puerta
                    drawContext.canvas.nativeCanvas.save()
                    drawContext.canvas.nativeCanvas.rotate(90f, doorCenterX, doorCenterY)
                    drawContext.canvas.nativeCanvas.drawText("PUERTA", doorCenterX, textY, textPaint)
                    drawContext.canvas.nativeCanvas.restore()






                    // Dibuja los escritorios y sillas de los estudiantes
                    val deskWidth = 70f
                    val deskHeight = 35f
                    val seatRadius = 10f
                    val rows = 8
                    val columns = 5
                    val spacing = 20f

                    // Calcular el offset inicial para centrar los escritorios en el aula
                    val totalDesksWidth = columns * deskWidth + (columns - 1) * spacing
                    val totalDesksHeight = rows * deskHeight + (rows - 1) * spacing
                    val initialOffsetX = (aulaWidth - totalDesksWidth) / 2 + paddingPx
                    val initialOffsetY = (aulaHeight - totalDesksHeight) / 2 + paddingPx + pizarraHeight

                    for (row in 0 until rows) {
                        for (column in 0 until columns) {
                            val offsetX = initialOffsetX + column * (deskWidth + spacing)
                            val offsetY = initialOffsetY + row * (deskHeight + spacing)

                            // Verifica que el escritorio no se salga del aula
                            if (offsetX + deskWidth <= size.width - paddingPx && offsetY + deskHeight + seatRadius * 2 <= size.height - paddingPx) {
                                // Dibuja el escritorio
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(offsetX, offsetY),
                                    size = Size(deskWidth, deskHeight),
                                    style = Stroke(width = 5f)
                                )

                                // Dibuja las sillas
                                val seatOffsetY = offsetY + deskHeight + seatRadius / 2
                                drawCircle(
                                    color = Color.Black,
                                    radius = seatRadius,
                                    center = Offset(offsetX + seatRadius, seatOffsetY)
                                )
                                drawCircle(
                                    color = Color.Black,
                                    radius = seatRadius,
                                    center = Offset(offsetX + deskWidth - seatRadius, seatOffsetY)
                                )

                            }
                        }
                    }
                }

            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val circleRadiusPx = with(density) { 25.dp.toPx() }
                val randomX = Random.nextFloat() * (rectSize.width - 2 * circleRadiusPx) + circleRadiusPx
                val randomY = Random.nextFloat() * (rectSize.height - 2 * circleRadiusPx) + circleRadiusPx
                circlePosition = Offset(randomX, randomY)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
            ) {
            Text("Actualizar ubicacion actual")
        }

    }

}




fun createRectanglePath(width: Float, height: Float, padding: Float): Path {
    return Path().apply {
        reset()
        moveTo(padding, padding)
        lineTo(width + padding, padding)
        lineTo(width + padding, height + padding)
        lineTo(padding, height + padding)
        close()
    }
}
