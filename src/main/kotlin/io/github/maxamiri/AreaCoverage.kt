// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Tracks and computes network coverage over a simulation area with temporal history.
 *
 * This class maintains a grid representation of the simulation area and tracks which
 * cells have been covered by mobile sinks over time. It supports both instantaneous
 * and historical coverage metrics, with the ability to track coverage over a sliding
 * time window.
 *
 * Coverage is determined by a circular communication radius around each mobile sink's
 * position. The class uses a precomputed disk pattern for efficient coverage updates.
 *
 * @property x Width of the simulation area in meters.
 * @property y Height of the simulation area in meters.
 * @property radius Communication radius of mobile sinks in meters.
 * @property history Number of time steps to track in the history window (1 for no history).
 */
class AreaCoverage(private val x: Int, private val y: Int, private val radius: Int, private val history: Int) {
    private val coverageHistory: Array<Array<BooleanArray>> = Array(history) { Array(x) { BooleanArray(y) } }
    private val disk: Array<BooleanArray> = precomputeDisk()

    /**
     * Precomputes a circular disk pattern for efficient coverage marking.
     *
     * Creates a square grid containing a circular pattern based on the communication
     * radius. This pattern is reused for each coverage update operation.
     *
     * @return A 2D boolean array representing the circular coverage pattern.
     */
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

    /**
     * Marks the area around a point as covered at a specific time.
     *
     * Updates the coverage grid by marking all cells within the communication radius
     * of the specified point as covered. If the time has advanced to a new history
     * slot, the slot is cleared before marking new coverage.
     *
     * @param cx X-coordinate of the mobile sink's center position.
     * @param cy Y-coordinate of the mobile sink's center position.
     * @param time Current simulation time step.
     */
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

    /**
     * Retrieves the coverage status of a specific cell at a given time.
     *
     * @param x X-coordinate of the cell.
     * @param y Y-coordinate of the cell.
     * @param time Time step to query.
     * @return True if the cell was covered at the specified time, false otherwise.
     */
    fun getValue(x: Int, y: Int, time: Int): Boolean {
        return coverageHistory[time % history][x][y]
    }

    /**
     * Prints the coverage grid for a specific time step (for debugging).
     *
     * Outputs a grid representation where '1' indicates covered cells and '0'
     * indicates uncovered cells.
     *
     * @param time The time step to print.
     */
    fun printCoverage(time: Int) {
        val currentCoverage = coverageHistory[time % history]
        currentCoverage.forEach { row ->
            println(row.joinToString(" ") { if (it) "1" else "0" })
        }
    }

    /**
     * Computes the coverage percentage for a specific time step.
     *
     * Calculates what percentage of the total simulation area was covered at
     * the specified time.
     *
     * @param time The time step to analyze.
     * @return Coverage percentage (0-100).
     */
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

    /**
     * Computes the coverage percentage across the entire history window.
     *
     * Calculates what percentage of the simulation area was covered at least once
     * during any time step in the history window. This provides a measure of
     * cumulative coverage over time.
     *
     * @return Cumulative coverage percentage (0-100) across all history slots.
     */
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

