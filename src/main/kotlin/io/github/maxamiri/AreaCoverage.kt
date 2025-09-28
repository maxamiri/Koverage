// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.math.pow
import kotlin.math.sqrt

class AreaCoverage(private val x: Int, private val y: Int, private val radius: Int, private val history: Int) {
    private val coverageHistory: Array<Array<BooleanArray>> = Array(history) { Array(x) { BooleanArray(y) } }
    private val disk: Array<BooleanArray> = precomputeDisk()

    // Precompute a square disk with the circle's shape inside
    private fun precomputeDisk(): Array<BooleanArray> {
        val size = 2 * radius + 1 // Size of the square
        val disk = Array(size) { BooleanArray(size) }

        for (i in 0 until size) {
            for (j in 0 until size) {
                val dx = i - radius
                val dy = j - radius
                disk[i][j] = dx * dx + dy * dy <= radius * radius
            }
        }
        return disk
    }

    // Method to set members inside the circle with radius R around (cx, cy) to true for a specific time
    private var previousCoverageIndex = -1
    fun point(cx: Int, cy: Int, time: Int) {
        // Determine the current coverage based on time
        val coverageIndex = time % history

        // Reset a coverageHistory when time advances
        if (coverageIndex != previousCoverageIndex){
            for (i in coverageHistory[coverageIndex].indices) {
                for (j in coverageHistory[coverageIndex][i].indices) {
                    coverageHistory[coverageIndex][i][j] = false
                }
            }
            previousCoverageIndex = coverageIndex
        }

        val startX = (cx - radius).coerceAtLeast(0)
        val startY = (cy - radius).coerceAtLeast(0)
        val endX = (cx + radius).coerceAtMost(x - 1)
        val endY = (cy + radius).coerceAtMost(y - 1)

        for (i in startX..endX) {
            for (j in startY..endY) {
                val diskX = i - (cx - radius)
                val diskY = j - (cy - radius)

                // Apply the disk only if within bounds
                if (diskX in disk.indices && diskY in disk[0].indices && disk[diskX][diskY]) {
                    coverageHistory[coverageIndex][i][j] = true
                }
            }
        }
    }

    // Get the value from a specific time copy
    fun getValue(x: Int, y: Int, time: Int): Boolean {
        return coverageHistory[time % history][x][y]
    }

    // Print a specific time copy of the grid for debugging
    fun printCoverage(time: Int) {
        val currentCoverage = coverageHistory[time % history]
        currentCoverage.forEach { row ->
            println(row.joinToString(" ") { if (it) "1" else "0" })
        }
    }

    // Compute the percentage of members set to true for a specific time
    fun coveragePercentage(time: Int): Double {
        val currentCoverage = coverageHistory[time % history]
        var trueCount = 0

        for (i in 0 until x) {
            for (j in 0 until y) {
                if (currentCoverage[i][j]) trueCount++
            }
        }

        return (trueCount.toDouble() / (x * y)) * 100
    }

    // Compute the average coverage percentage across all history copies
    fun coveragePercentageHistory(): Double {
        var trueCount = 0
        for (i in 0 until x) {
            for (j in 0 until y) {
                for(h in 0 until history){
                    if (coverageHistory[h][i][j]){
                        trueCount++
                        break
                    }
                }
            }
        }

        // Calculate the percentage of true values in the summary
        return (trueCount.toDouble() / (x * y)) * 100
    }
}
