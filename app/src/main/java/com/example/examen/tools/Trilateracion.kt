package com.example.examen.tools

import com.example.examen.Cardinal
import kotlin.math.pow

fun trilateration(p1: Cardinal, p2: Cardinal, p3: Cardinal, d1: Double, d2: Double, d3: Double): Cardinal {
    val A = 2 * (p2.x - p1.x)
    val B = 2 * (p2.y - p1.y)
    val C = d1.pow(2) - d2.pow(2) - p1.x.pow(2) + p2.x.pow(2) - p1.y.pow(2) + p2.y.pow(2)
    val D = 2 * (p3.x - p2.x)
    val E = 2 * (p3.y - p2.y)
    val F = d2.pow(2) - d3.pow(2) - p2.x.pow(2) + p3.x.pow(2) - p2.y.pow(2) + p3.y.pow(2)

    val x = (C * E - F * B) / (E * A - B * D)
    val y = (C * D - A * F) / (B * D - A * E)

    return Cardinal(x, y)

}