// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import java.io.File
import kotlin.random.Random
import kotlinx.coroutines.*
import java.util.concurrent.Executors

// Define a Point class to represent x and y coordinates
data class Point(var x: Double, var y: Double)

// Define an Area class with width and height
data class Area(val width: Int, val height: Int) {
    val totalArea: Double
        get() = width * height.toDouble()
}

// Define a Car class with its current position and mobility model
class Car(val id: Int, var position: Point, var mobilityModel: MobilityModel) {
    fun move() {
        mobilityModel.move(this)
    }
}

// Data class to hold configuration for each simulation
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
    val logFile: String
)

// Simulator class
class Simulator(private val config: SimulationConfig) {
    private val cars = mutableListOf<Car>()
    private val coverage = if (config.history>0) {
        AreaCoverage(config.area.width, config.area.height, config.radius.toInt(), config.history)
    }
    else {
        AreaCoverage(config.area.width, config.area.height, config.radius.toInt(), 1)
    }
    private val random = Random(config.seed) // One Random instance per simulation

    init {
        repeat(config.carCount) { i ->
            val initialPosition = Point(
                random.nextDouble(config.area.width.toDouble()),
                random.nextDouble(config.area.height.toDouble())
            )
            // println("Car $i @ initialPosition: ${initialPosition.x}, ${initialPosition.y}")
            val mobilityModel = when (config.mobilityModelType) {
                "RandomWaypoint" -> RandomWaypoint(
                    config.area,
                    config.tripMinSpeed,
                    config.tripMaxSpeed,
                    config.waitTime,
                    random // Pass simulation's random instance
                )
                "RandomDirection" -> RandomDirection(
                    config.area,
                    config.tripMinSpeed,
                    config.tripMaxSpeed,
                    config.waitTime,
                    random // Pass simulation's random instance
                )
                "Enterprise" -> Enterprise(
                    config.area,
                    config.tripMinSpeed,
                    config.tripMaxSpeed,
                    config.waitTime,
                    random // Pass simulation's random instance
                )
                else -> throw IllegalArgumentException("Unsupported mobility model")
            }
            cars.add(Car(i, initialPosition, mobilityModel))
        }
    }

    fun run(): Double {
        val coveragePercentage:MutableList<Double> = mutableListOf()
        var logging = false
        var logBuffer = ""
        if (config.logFile != "" ){
            logging = true
        }

        for (time in 0 until config.duration) {
            cars.forEach {
                it.move()
                if(config.history>0) {
                    coverage.point(it.position.x.toInt(),it.position.y.toInt(),time)
                }
                else {
                    coverage.point(it.position.x.toInt(),it.position.y.toInt(),0)
                }
                if (logging) {
                    logBuffer += "${it.id},$time,${it.position.x},${it.position.y}\n"
                }
            }
            if (config.history in 1..time){
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
        return if (config.history>0){
            coveragePercentage.average()
        } else{
            coverage.coveragePercentageHistory()
        }

    }
}

// Class to load configurations, run simulations, and save results
class SimulationRunner(configFile: String, resultFile: String) {
    private val configs = loadConfigs(configFile)
    private val resultFile = File(resultFile)

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
                    logFile = parts[11]
                )
            }
    }


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

                        results += "${config.mobilityModelType},${config.seed},${config.carCount},${config.radius},${config.logFile},${
                            "%.2f".format(coveragePercentage/counter)
                        }\n"
                    } catch (e: Exception) {
                        println("ERROR: Simulation failed for ${config.mobilityModelType},${config.seed},${config.carCount},${config.radius}. Error: ${e.message}")
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

// Main function to run simulations from configurations
fun main() {
    val configFile = "simulation_configs.csv"
    val resultFile = "simulation_results.csv"
    val runner = SimulationRunner(configFile, resultFile)
    runner.runAllSimulations()
    println("Simulations completed. Results saved in $resultFile")
}
