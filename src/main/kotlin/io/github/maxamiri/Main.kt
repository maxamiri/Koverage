// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * Represents a 2D point in the simulation space.
 *
 * @property x The x-coordinate of the point (mutable).
 * @property y The y-coordinate of the point (mutable).
 */
data class Point(var x: Double, var y: Double)

/**
 * Represents a rectangular simulation area.
 *
 * @property width The width of the area in meters.
 * @property height The height of the area in meters.
 */
data class Area(val width: Int, val height: Int) {
    /**
     * The total area in square meters.
     */
    val totalArea: Double
        get() = width * height.toDouble()
}

/**
 * Represents a mobile sink (vehicle, AGV, or drone) in the simulation.
 *
 * A car moves through the simulation area according to its assigned mobility model,
 * providing network coverage within its communication radius.
 *
 * @property id Unique identifier for this car.
 * @property position Current position of the car in the simulation area.
 * @property mobilityModel The mobility model governing this car's movement.
 */
class Car(val id: Int, var position: Point, var mobilityModel: MobilityModel) {
    /**
     * Moves the car according to its mobility model.
     *
     * This method delegates movement to the assigned [mobilityModel].
     */
    fun move() {
        mobilityModel.move(this)
    }
}

/**
 * Configuration parameters for a single simulation run.
 *
 * @property carCount Number of mobile sinks (cars) in the simulation.
 * @property area The simulation area dimensions.
 * @property duration Duration of the simulation in time steps (seconds).
 * @property radius Communication radius of each mobile sink in meters.
 * @property mobilityModelType Type of mobility model to use ("RandomWaypoint", "RandomDirection", or "Enterprise").
 * @property tripMinSpeed Minimum speed in meters per second.
 * @property tripMaxSpeed Maximum speed in meters per second.
 * @property waitTime Wait time in seconds after reaching a destination or boundary.
 * @property seed Random seed for reproducible simulations.
 * @property history Number of time steps to track for coverage history (-1 for no history tracking).
 * @property logFile Path to save position log file (empty string to disable logging).
 */
data class SimulationConfig(
    val carCount: Int,
    val area: Area,
    val duration: Int,
    val radius: Double,
    val mobilityModelType: String, // "RandomWaypoint" or "RandomDirection"
    val tripMinSpeed: Double,
    val tripMaxSpeed: Double,
    val waitTime: Int,
    var seed: Long,
    val history: Int,
    val logFile: String,
)

/**
 * Main simulation engine for network coverage evaluation.
 *
 * The Simulator manages the movement of mobile sinks and tracks the area coverage
 * over time. It supports various mobility models and can optionally log position data.
 *
 * @property config The configuration parameters for this simulation.
 */
class Simulator(private val config: SimulationConfig) {
    private val cars = mutableListOf<Car>()
    private val coverage = if (config.history > 0) {
        AreaCoverage(config.area.width, config.area.height, config.radius.toInt(), config.history)
    } else {
        AreaCoverage(config.area.width, config.area.height, config.radius.toInt(), 1)
    }
    private val random = Random(config.seed) // One Random instance per simulation

    init {
        repeat(config.carCount) { i ->
            val initialPosition = Point(
                random.nextDouble(config.area.width.toDouble()),
                random.nextDouble(config.area.height.toDouble()),
            )
            // println("Car $i @ initialPosition: ${initialPosition.x}, ${initialPosition.y}")
            val mobilityModel = when (config.mobilityModelType) {
                "RandomWaypoint" -> RandomWaypoint(
                    config.area,
                    config.tripMinSpeed,
                    config.tripMaxSpeed,
                    config.waitTime,
                    random, // Pass simulation's random instance
                )
                "RandomDirection" -> RandomDirection(
                    config.area,
                    config.tripMinSpeed,
                    config.tripMaxSpeed,
                    config.waitTime,
                    random, // Pass simulation's random instance
                )
                "Enterprise" -> Enterprise(
                    config.area,
                    config.tripMinSpeed,
                    config.tripMaxSpeed,
                    config.waitTime,
                    random, // Pass simulation's random instance
                )
                else -> throw IllegalArgumentException("Unsupported mobility model")
            }
            cars.add(Car(i, initialPosition, mobilityModel))
        }
    }

    /**
     * Executes the simulation for the configured duration.
     *
     * This method moves all cars at each time step, tracks coverage, and optionally
     * logs position data to a file.
     *
     * @return The coverage percentage. If history tracking is enabled, returns the
     *         average coverage percentage across the history window. Otherwise,
     *         returns the total coverage percentage at the end of simulation.
     */
    fun run(): Double {
        val coveragePercentage: MutableList<Double> = mutableListOf()
        var logging = false
        var logBuffer = ""
        if (config.logFile != "") {
            logging = true
        }

        for (time in 0 until config.duration) {
            cars.forEach {
                it.move()
                if (config.history > 0) {
                    coverage.point(it.position.x.toInt(), it.position.y.toInt(), time)
                } else {
                    coverage.point(it.position.x.toInt(), it.position.y.toInt(), 0)
                }
                if (logging) {
                    logBuffer += "${it.id},$time,${it.position.x},${it.position.y}\n"
                }
            }
            if (config.history in 1..time) {
                coveragePercentage.add(coverage.coveragePercentageHistory())
                // println("Coverage:"  + coveragePercentage.joinToString())
            }
        }

        if (logging) {
            File(config.logFile).bufferedWriter().use { writer ->
                writer.write("id,t,x,y\n")
                writer.write(logBuffer)
            }
        }

        cars.clear()
        return if (config.history > 0) {
            coveragePercentage.average()
        } else {
            coverage.coveragePercentageHistory()
        }
    }
}

/**
 * Manages loading, execution, and result collection for multiple simulations.
 *
 * The SimulationRunner reads simulation configurations from a CSV file, executes
 * them in parallel using coroutines, and saves the aggregated results to a file.
 * Each configuration is run multiple times with different random seeds to obtain
 * statistically significant coverage metrics.
 *
 * @property configFile Path to the CSV file containing simulation configurations.
 * @property resultFile Path where simulation results will be saved.
 */
class SimulationRunner(configFile: String, resultFile: String) {
    private val configs = loadConfigs(configFile)
    private val resultFile = File(resultFile)

    /**
     * Loads simulation configurations from a CSV file.
     *
     * The CSV file should have the following format (without headers):
     * carCount, area.x, area.y, duration, radius, mobility, minSpeed, maxSpeed, waitTime, seed, history, logFile
     *
     * Lines starting with '#' are treated as comments and ignored.
     *
     * @param configFile Path to the CSV configuration file.
     * @return List of parsed [SimulationConfig] objects.
     */
    private fun loadConfigs(configFile: String): List<SimulationConfig> {
        return File(configFile)
            .readLines()
            .filter { it.isNotBlank() && !it.trimStart().startsWith("#") } // Skip blank lines and lines starting with #
            .map { line ->
                val parts = line.split(",")
                SimulationConfig(
                    carCount = parts[0].toInt(),
                    area = Area(parts[1].toInt(), parts[2].toInt()),
                    duration = parts[3].toInt(),
                    radius = parts[4].toDouble(),
                    mobilityModelType = parts[5],
                    tripMinSpeed = parts[6].toDouble(),
                    tripMaxSpeed = parts[7].toDouble(),
                    waitTime = parts[8].toInt(),
                    seed = parts[9].toLong(),
                    history = parts[10].toInt(),
                    logFile = parts[11],
                )
            }
    }

    /**
     * Executes all configured simulations in parallel.
     *
     * Each configuration is run 1000 times with incrementing random seeds to compute
     * an average coverage percentage. Simulations are executed concurrently using
     * a thread pool of up to 12 threads. Results are written to the result file with
     * the format: mobilityModel, finalSeed, carCount, radius, logFile, avgCoverage
     */
    fun runAllSimulations() {
        val dispatcher = Executors.newFixedThreadPool(12).asCoroutineDispatcher() // Up to 12 parallel simulations
        runBlocking(dispatcher) {
            var results = ""
            configs.map { config ->
                async {
                    try {
                        val counter = 1000
                        var coveragePercentage = 0.0
                        for (i in 0 until counter) {
                            val simulator = Simulator(config)
                            config.seed += 10
                            coveragePercentage += simulator.run()
                        }

                        val avg = "%.2f".format(coveragePercentage / counter)
                        results += listOf(
                            config.mobilityModelType,
                            config.seed.toString(),
                            config.carCount.toString(),
                            config.radius.toString(),
                            config.logFile,
                            avg,
                        ).joinToString(",") + "\n"
                    } catch (e: Exception) {
                        println("ERROR: Simulation failed for ${config.mobilityModelType}, ${config.seed}.")
                        println("Details: carCount=${config.carCount}, radius=${config.radius}, error=${e.message}")
                    }
                }
            }.awaitAll()

            resultFile.bufferedWriter().use { writer ->
                writer.write(results)
                writer.flush() // Immediately flush to save results
            }
        }
        dispatcher.close()
    }
}

/**
 * Entry point for the Koverage simulation application.
 *
 * Loads configurations from 'simulation_configs.csv', executes all simulations,
 * and saves results to 'simulation_results.csv'.
 */
fun main() {
    val configFile = "simulation_configs.csv"
    val resultFile = "simulation_results.csv"
    val runner = SimulationRunner(configFile, resultFile)
    runner.runAllSimulations()
    println("Simulations completed. Results saved in $resultFile")
}
