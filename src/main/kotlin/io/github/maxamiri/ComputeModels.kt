// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.math.exp

// Function to get the direction model parameters
fun getDirectionModelParams(speed: Double): Pair<Double, Double> {
    return when {
        speed > 0 && speed < 20 -> Pair(0.3679, 0.0647)
        speed in 20.0..<40.0 -> Pair(0.5312, 0.0982)
        speed in 40.0..<60.0 -> Pair(0.7148, 0.0999)
        speed >= 60.0 -> Pair(0.7830, 0.0999)
        else -> throw IllegalArgumentException("Speed $speed does not belong to any valid range.")
    }
}

// Function to get the speed model parameters
fun getSpeedModelParams(speed: Double): Pair<Double, Double> {
    return when {
        speed > 0 && speed < 20 -> Pair(0.7186, 0.0999)
        speed in 20.0..<40.0 -> Pair(0.7443, 0.0999)
        speed in 40.0..<60.0 -> Pair(0.5538, 0.0896)
        speed >= 60.0 -> Pair(0.5937, 0.0999)
        else -> throw IllegalArgumentException("Speed $speed does not belong to any valid range.")
    }
}

// Function to compute the exponential model
fun computeExponentialModel(a: Double, b: Double, x: Double): Double {
    return a * exp(-b * x)
}

// Function to normalise a direction
fun normaliseDirection(a: Double, b: Double): Double {
    val fxSum = (0..180).sumOf { x -> computeExponentialModel(a, b, x.toDouble()) }
    return fxSum
}

// Function to normalise speed
fun normaliseSpeed(a: Double, b: Double): Double {
    val fxSum = (0..80).sumOf { x -> computeExponentialModel(a, b, x.toDouble()) }
    return fxSum
}

fun main() {
    // Define representative speeds for each class
    val speeds = listOf(1.0, 20.0, 40.0, 60.0)

    // Compute a normalised direction (N) for all direction model parameters
    println("Normalised Direction (N):")
    speeds.forEach { speed ->
        val params = getDirectionModelParams(speed)
        val (a, b) = params
        val normalisedValue = normaliseDirection(a, b)
        println("For speed $speed: a = $a, b = $b, N = $normalisedValue")
    }

    // Compute normalised speed (N) for all speed model parameters
    println("\nNormalised Speed (N):")
    speeds.forEach { speed ->
        val params = getSpeedModelParams(speed)
        val (a, b) = params
        val normalisedValue = normaliseSpeed(a, b)
        println("For speed $speed: a = $a, b = $b, N = $normalisedValue")
    }
}
