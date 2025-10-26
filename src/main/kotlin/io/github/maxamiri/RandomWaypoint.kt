// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Random Waypoint mobility model implementation.
 *
 * In this model, a mobile node selects a random destination (waypoint) within the
 * simulation area, travels to it at a randomly chosen speed, and optionally pauses
 * for a specified wait time before selecting the next waypoint.
 *
 * This implements the classic Random Waypoint model.
 *
 * @property area The simulation area boundaries.
 * @property tripMinSpeed Minimum travel speed in meters per second.
 * @property tripMaxSpeed Maximum travel speed in meters per second.
 * @property waitTime Duration to pause at each waypoint in seconds.
 * @property random Random number generator for waypoint selection and speed.
 */
class RandomWaypoint(
    area: Area,
    private val tripMinSpeed: Double,
    private val tripMaxSpeed: Double,
    private val waitTime: Int,
    random: Random,
) : MobilityModel(area, random) {
    private var target: Point? = null
    private var remainingTripTime = 0
    private var speedX = 0.0
    private var speedY = 0.0
    private var currentWaitTime = 0

    /**
     * Moves the car one time step according to the Random Waypoint model.
     *
     * The car either:
     * - Continues traveling toward its current waypoint
     * - Waits at the waypoint if the wait time has not elapsed
     * - Selects a new random waypoint and begins traveling to it
     *
     * @param car The car to move.
     */
    override fun move(car: Car) {
        if (remainingTripTime > 0) {
            remainingTripTime -= 1
            car.position.x += speedX
            car.position.y += speedY
        } else if (currentWaitTime > 0) {
            currentWaitTime -= 1
        } else {
            target = generateRandomPoint()
            val dx = target!!.x - car.position.x
            val dy = target!!.y - car.position.y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance > 0) {
                val speed = tripMinSpeed + random.nextDouble() * (tripMaxSpeed - tripMinSpeed)
                remainingTripTime = (distance / speed).toInt()
                speedX = dx / remainingTripTime
                speedY = dy / remainingTripTime
                currentWaitTime = waitTime
            } else {
                remainingTripTime = 0
                speedX = 0.0
                speedY = 0.0
            }
        }
    }

    /**
     * Generates a random point within the simulation area.
     *
     * @return A new Point with random coordinates within the area boundaries.
     */
    private fun generateRandomPoint(): Point {
        val x = random.nextDouble(area.width.toDouble())
        val y = random.nextDouble(area.height.toDouble())
        return Point(x, y)
    }
}
