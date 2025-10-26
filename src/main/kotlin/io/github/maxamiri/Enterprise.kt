// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.math.*
import kotlin.random.Random

/**
 * Specialized Synthetic Mobility Model based on real-world trace data.
 *
 * This mobility model is designed to reflect structured vehicular motion in constrained
 * environments such as industrial yards or logistics zones. It uses exponential
 * distributions parameterized from real-world GPS traces to generate realistic
 * speed and direction changes.
 *
 * The model updates speed and direction every 5 seconds based on probability
 * distributions that depend on the current speed class. This implements the
 * Enterprise/Synthetic model presented in:
 *
 * Amiri, M., Eyers, D., & Huang, Z. (2024). A Specialised Synthetic Mobility Model
 * Based on Real-World Traces. In 2024 34th International Telecommunication Networks
 * and Applications Conference (ITNAC) (pp. 1-5). IEEE.
 *
 * @property area The simulation area boundaries.
 * @property tripMinSpeed Minimum travel speed in meters per second.
 * @property tripMaxSpeed Maximum travel speed in meters per second.
 * @property waitTime Duration to pause (currently unused in this implementation).
 * @property random Random number generator for stochastic decisions.
 */
class Enterprise(
    area: Area,
    private val tripMinSpeed: Double,
    private val tripMaxSpeed: Double,
    private val waitTime: Int,
    random: Random
) : MobilityModel(area, random) {

    /**
     * Parameters for an exponential probability distribution class.
     *
     * Each speed class (e.g., 0-20 km/h, 20-40 km/h) has its own exponential
     * distribution parameters derived from real-world trace data.
     *
     * @property min Minimum value of the speed class in km/h.
     * @property max Maximum value of the speed class in km/h.
     * @property range Valid output values for this distribution.
     * @property a Exponential distribution parameter (amplitude).
     * @property b Exponential distribution parameter (decay rate).
     */
    data class ClassParams(
        val min: Int,
        val max: Int,
        val range: List<Int>,
        val a: Double,
        val b: Double
    ) {
        /**
         * Normalized probability values for each element in [range].
         */
        val probabilities: List<Double>

        /**
         * Normalization constant for the exponential distribution.
         */
        var normalise:Double

        init {
            normalise = range.sumOf { x -> exponential(a, b, x.toDouble()) }
            probabilities = range.map { x -> exponential(a, b, x.toDouble()) / normalise }
        }

        /**
         * Computes the exponential distribution function: a * exp(-b * x).
         *
         * @param a Amplitude parameter.
         * @param b Decay rate parameter.
         * @param x Input value.
         * @return The exponential function value.
         */
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

    /**
     * Moves the car one time step according to the Enterprise mobility model.
     *
     * On the first call, initializes random speed and direction. Subsequently,
     * updates position continuously and recalculates speed/direction every 5 seconds
     * based on exponential probability distributions. Handles boundary conditions
     * by reflecting the car back into the simulation area.
     *
     * @param car The car to move.
     */
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

    /**
     * Calculates the x and y velocity components from speed and direction.
     *
     * Converts polar coordinates (speed and direction) to Cartesian velocity components.
     */
    private fun calculateSpeedComponents() {
        val radians = Math.toRadians(direction.toDouble())
        speedX = speed * cos(radians)
        speedY = speed * sin(radians)
    }

    /**
     * Generates a new speed change value based on the current speed.
     *
     * Uses the speed-dependent exponential distribution to determine the magnitude
     * of speed change (in m/s).
     *
     * @param previousSpeed The current speed in meters per second.
     * @return The speed change magnitude in meters per second.
     */
    private fun generateNewSpeed(previousSpeed: Int): Int {
        // Generate new speed and direction
        val newSpeed = kmh2mps(generateValue(mps2kmh(previousSpeed), speedParams))
        // println("Previous speed: $previousSpeed, change speed: $newSpeed")
        return newSpeed
    }

    /**
     * Generates a new direction change value based on the current speed.
     *
     * Uses the speed-dependent exponential distribution to determine the magnitude
     * of direction change (in degrees).
     *
     * @param previousSpeed The current speed in meters per second.
     * @return The direction change magnitude in degrees.
     */
    private fun generateNewDirection(previousSpeed: Int): Int {
        val newDirection = generateValue(mps2kmh(previousSpeed), directionParams)
        // println("Previous speed: $previousSpeed, change direction: $newDirection")
        return newDirection
    }

    /**
     * Converts kilometers per hour to meters per second.
     *
     * @param x Speed in km/h.
     * @return Speed in m/s.
     */
    private fun kmh2mps(x:Int) =
        (x/3.6).toInt()

    /**
     * Converts meters per second to kilometers per hour.
     *
     * @param x Speed in m/s.
     * @return Speed in km/h.
     */
    private fun mps2kmh(x:Int) =
        (x*3.6).toInt()

    /**
     * Speed change distribution parameters for different speed classes.
     *
     * Each entry defines the exponential distribution parameters for a specific
     * speed range (in km/h). Parameters are derived from real-world trace analysis.
     */
    val speedParams = listOf(
        ClassParams(0, 20, (0..80).toList(), 0.7186, 0.0999),
        ClassParams(20, 40, (0..80).toList(), 0.7443, 0.0999),
        ClassParams(40, 60, (0..80).toList(), 0.5538, 0.0896),
        ClassParams(60, 100, (0..80).toList(), 0.5937, 0.0999)
    )

    /**
     * Direction change distribution parameters for different speed classes.
     *
     * Each entry defines the exponential distribution parameters for a specific
     * speed range (in km/h). Parameters are derived from real-world trace analysis.
     */
    val directionParams = listOf(
        ClassParams(0, 20, (0..180).toList(), 0.3679, 0.0647),
        ClassParams(20, 40, (0..180).toList(), 0.5312, 0.0982),
        ClassParams(40, 60, (0..180).toList(), 0.7148, 0.0999),
        ClassParams(60, 100, (0..180).toList(), 0.7830, 0.0999)
    )

    /**
     * Generates a random value from the appropriate exponential distribution.
     *
     * Selects the appropriate [ClassParams] based on the input value, then samples
     * from the corresponding probability distribution using inverse transform sampling.
     *
     * @param x The current value (speed in km/h) used to select the distribution class.
     * @param classParamsList List of distribution parameters for different classes.
     * @return A randomly sampled value from the selected distribution.
     * @throws IllegalStateException if no matching class parameters are found.
     */
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

