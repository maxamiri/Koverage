// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.math.exp

/**
 * Retrieves the exponential distribution parameters for direction changes.
 *
 * Returns the (a, b) parameters for the exponential distribution based on the
 * current speed class. These parameters are derived from real-world trace analysis
 * for the Enterprise mobility model.
 *
 * @param speed Current speed in km/h.
 * @return Pair of (a, b) parameters for the exponential distribution.
 * @throws IllegalArgumentException if the speed is outside valid ranges.
 */
fun getDirectionModelParams(speed: Double): Pair<Double, Double> {
    return when {
        speed > 0 && speed < 20 -> Pair(0.3679, 0.0647)
        speed in 20.0..<40.0 -> Pair(0.5312, 0.0982)
        speed in 40.0..<60.0 -> Pair(0.7148, 0.0999)
        speed >= 60.0 -> Pair(0.7830, 0.0999)
        else -> throw IllegalArgumentException("Speed $speed does not belong to any valid range.")
    }
}

/**
 * Retrieves the exponential distribution parameters for speed changes.
 *
 * Returns the (a, b) parameters for the exponential distribution based on the
 * current speed class. These parameters are derived from real-world trace analysis
 * for the Enterprise mobility model.
 *
 * @param speed Current speed in km/h.
 * @return Pair of (a, b) parameters for the exponential distribution.
 * @throws IllegalArgumentException if the speed is outside valid ranges.
 */
fun getSpeedModelParams(speed: Double): Pair<Double, Double> {
    return when {
        speed > 0 && speed < 20 -> Pair(0.7186, 0.0999)
        speed in 20.0..<40.0 -> Pair(0.7443, 0.0999)
        speed in 40.0..<60.0 -> Pair(0.5538, 0.0896)
        speed >= 60.0 -> Pair(0.5937, 0.0999)
        else -> throw IllegalArgumentException("Speed $speed does not belong to any valid range.")
    }
}

/**
 * Computes the value of an exponential distribution function.
 *
 * Calculates: f(x) = a * exp(-b * x)
 *
 * @param a Amplitude parameter of the exponential distribution.
 * @param b Decay rate parameter of the exponential distribution.
 * @param x Input value.
 * @return The exponential function value at x.
 */
fun computeExponentialModel(a: Double, b: Double, x: Double): Double {
    return a * exp(-b * x)
}

/**
 * Computes the normalization constant for direction change distributions.
 *
 * Sums the exponential distribution values over the valid direction change
 * range (0-180 degrees) to obtain the normalization constant.
 *
 * @param a Amplitude parameter of the exponential distribution.
 * @param b Decay rate parameter of the exponential distribution.
 * @return The normalization constant (sum of distribution values).
 */
fun normaliseDirection(a: Double, b: Double): Double {
    val fxSum = (0..180).sumOf { x -> computeExponentialModel(a, b, x.toDouble()) }
    return fxSum
}

/**
 * Computes the normalization constant for speed change distributions.
 *
 * Sums the exponential distribution values over the valid speed change
 * range (0-80 km/h) to obtain the normalization constant.
 *
 * @param a Amplitude parameter of the exponential distribution.
 * @param b Decay rate parameter of the exponential distribution.
 * @return The normalization constant (sum of distribution values).
 */
fun normaliseSpeed(a: Double, b: Double): Double {
    val fxSum = (0..80).sumOf { x -> computeExponentialModel(a, b, x.toDouble()) }
    return fxSum
}

/**
 * Utility function to compute and display normalization constants.
 *
 * This main function calculates and prints the normalization constants for
 * both direction and speed distributions across all speed classes. It's used
 * for validating the Enterprise mobility model parameters.
 */
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
