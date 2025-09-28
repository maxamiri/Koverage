// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.math.*
import kotlin.random.Random

// Enterprise mobility model implementation with speed and wait time
class Enterprise(
    area: Area,
    private val tripMinSpeed: Double,
    private val tripMaxSpeed: Double,
    private val waitTime: Int,
    random: Random
) : MobilityModel(area, random) {

    data class ClassParams(
        val min: Int,
        val max: Int,
        val range: List<Int>,
        val a: Double,
        val b: Double
    ) {
        val probabilities: List<Double>
        var normalise:Double

        init {
            normalise = range.sumOf { x -> exponential(a, b, x.toDouble()) }
            probabilities = range.map { x -> exponential(a, b, x.toDouble()) / normalise }
        }

        private fun exponential(a: Double, b: Double, x: Double): Double {
            return a * exp(-b * x)
        }
    }

    private var firstRun = true
    private var speed = 0
    private var direction = 0
    private var remainingTripTime = 0
    private var speedX = 0.0
    private var speedY = 0.0
    private var previousSpeedSign = 0
    private var previousDirectionSign = 0

    override fun move(car: Car) {
        if (firstRun) {
            // Generate a random initial speed and direction
            speed = (tripMinSpeed + (tripMaxSpeed - tripMinSpeed) * random.nextDouble()).toInt()
            direction = random.nextInt(360)
            calculateSpeedComponents()
            remainingTripTime = 5 // Reset trip time to 5 seconds
            firstRun = false
        }
        // println("speedX:$speedX speedY:$speedY direction:$direction")

        // Update position
        car.position.x += speedX
        if(car.position.x<0) {
            car.position.x *= -1
            direction = (direction+180) % 360
            calculateSpeedComponents()
        }
        if(car.position.x>area.width) {
            car.position.x = 2*area.width - car.position.x
            direction = (direction+180) % 360
            calculateSpeedComponents()
        }

        car.position.y += speedY
        if(car.position.y<0) {
            car.position.y *= -1
            direction = (direction+180) % 360
            calculateSpeedComponents()
        }
        if(car.position.y>area.height) {
            car.position.y = 2*area.height - car.position.y
            direction = (direction+180) % 360
            calculateSpeedComponents()
        }

        // Decrease remaining trip time
        remainingTripTime--

        if (remainingTripTime == 0) {
            // Call generateNewSpeed and generateNewDirection
            val proposedSpeedChange = generateNewSpeed(speed) // mps to kmh
            val proposedDirectionChange = generateNewDirection(speed)
            remainingTripTime = 5

            // Apply proposed changes considering previous values
            if (proposedSpeedChange == 0) {
                previousSpeedSign = 0
            } else if ( previousSpeedSign == 0) {
                previousSpeedSign = if (random.nextBoolean()) 1 else -1
            }

            speed += (proposedSpeedChange * previousSpeedSign)
            //println("speed:$speed += (proposedSpeedChange: $proposedSpeedChange * previousSpeedSign: $previousSpeedSign)")

            // Adjust speed to desired range
            if (speed< tripMinSpeed) speed = tripMinSpeed.toInt()
            if (speed> tripMaxSpeed) speed = tripMaxSpeed.toInt()

            if (proposedDirectionChange == 0) {
                previousDirectionSign = 0
            } else if ( previousDirectionSign == 0) {
                previousDirectionSign = if (random.nextBoolean()) 1 else -1
            }

            direction += (proposedDirectionChange * previousDirectionSign)

            //println("direction:$direction += (proposedDirectionChange:$proposedDirectionChange * previousDirectionSign:$previousDirectionSign)")

            calculateSpeedComponents()

            //println("Final speed:$speed")
        }
    }

    // Helper to calculate speed components (x and y)
    private fun calculateSpeedComponents() {
        val radians = Math.toRadians(direction.toDouble())
        speedX = speed * cos(radians)
        speedY = speed * sin(radians)
    }

    // Implementation of generateNewSpeed and generateNewDirection
    private fun generateNewSpeed(previousSpeed: Int): Int {
        // Generate new speed and direction
        val newSpeed = kmh2mps(generateValue(mps2kmh(previousSpeed), speedParams))
        // println("Previous speed: $previousSpeed, change speed: $newSpeed")
        return newSpeed
    }

    private fun generateNewDirection(previousSpeed: Int): Int {
        val newDirection = generateValue(mps2kmh(previousSpeed), directionParams)
        // println("Previous speed: $previousSpeed, change direction: $newDirection")
        return newDirection
    }

    private fun kmh2mps(x:Int) =
        (x/3.6).toInt()

    private fun mps2kmh(x:Int) =
        (x*3.6).toInt()

    // Speed ClassParams
    val speedParams = listOf(
        ClassParams(0, 20, (0..80).toList(), 0.7186, 0.0999),
        ClassParams(20, 40, (0..80).toList(), 0.7443, 0.0999),
        ClassParams(40, 60, (0..80).toList(), 0.5538, 0.0896),
        ClassParams(60, 100, (0..80).toList(), 0.5937, 0.0999)
    )

    // Direction ClassParams
    val directionParams = listOf(
        ClassParams(0, 20, (0..180).toList(), 0.3679, 0.0647),
        ClassParams(20, 40, (0..180).toList(), 0.5312, 0.0982),
        ClassParams(40, 60, (0..180).toList(), 0.7148, 0.0999),
        ClassParams(60, 100, (0..180).toList(), 0.7830, 0.0999)
    )

    fun generateValue(x: Int, classParamsList: List<ClassParams>): Int {
        // Find the matching ClassParams
        val selectedClassParams = classParamsList.find { x in it.min until it.max }

        // If no matching range is found, return null
        selectedClassParams ?: throw IllegalStateException("Class params not found for $x")

        // Generate a random number
        val randomValue = Math.random()

        // Find the index corresponding to the random value
        var cumulativeSum = 0.0
        val selectedIndex = selectedClassParams.probabilities.indexOfFirst { probability ->
            cumulativeSum += probability
            cumulativeSum >= randomValue
        }.takeIf { it != -1 } ?: selectedClassParams.probabilities.lastIndex // Use the last index for edge cases

        // Return the corresponding number in the range
        return selectedClassParams.range[selectedIndex]
    }
}