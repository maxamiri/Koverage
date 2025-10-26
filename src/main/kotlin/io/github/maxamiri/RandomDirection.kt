// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Random Direction mobility model implementation.
 *
 * In this model, a mobile node travels in a randomly chosen direction at a randomly
 * chosen speed until it reaches a boundary of the simulation area. Upon reaching
 * a boundary, it pauses for a random wait time, then selects a new random direction
 * and speed and continues.
 *
 * This implements the Random Direction model.
 *
 * @property area The simulation area boundaries.
 * @property tripMinSpeed Minimum travel speed in meters per second.
 * @property tripMaxSpeed Maximum travel speed in meters per second.
 * @property maxWaitTime Maximum duration to pause at boundaries in seconds.
 * @property random Random number generator for direction and speed selection.
 */
class RandomDirection(
    area: Area,
    private val tripMinSpeed: Double,
    private val tripMaxSpeed: Double,
    private val maxWaitTime: Int, // Maximum wait time
    random: Random,
) : MobilityModel(area, random) {

    private var angle = random.nextDouble(0.0, 360.0)
    private var speed = tripMinSpeed + random.nextDouble() * (tripMaxSpeed - tripMinSpeed)
    private var currentWaitTime = 0

    /**
     * Moves the car one time step according to the Random Direction model.
     *
     * The car either:
     * - Waits if currently pausing at a boundary
     * - Continues moving in its current direction
     * - Handles boundary collision by stopping and selecting a new direction
     *
     * @param car The car to move.
     */
    override fun move(car: Car) {
        if (currentWaitTime > 0) {
            currentWaitTime -= 1
            return // Pausing after hitting a boundary
        }

        // Calculate the next position
        val newX = car.position.x + speed * cos(Math.toRadians(angle))
        val newY = car.position.y + speed * sin(Math.toRadians(angle))

        // Check if the boundary is reached
        if (newX < 0 || newX > area.width || newY < 0 || newY > area.height) {
            handleBoundaryHit(car)
        } else {
            // Continue moving within bounds
            car.position.x = newX
            car.position.y = newY
        }
    }

    /**
     * Handles the event when a car reaches the simulation area boundary.
     *
     * The car's position is clamped to the boundary, a random wait time is selected,
     * and a new random direction and speed are chosen for the next movement phase.
     *
     * @param car The car that reached the boundary.
     */
    private fun handleBoundaryHit(car: Car) {
        // Stop at boundary
        car.position.x = car.position.x.coerceIn(0.0, area.width.toDouble())
        car.position.y = car.position.y.coerceIn(0.0, area.height.toDouble())

        // Set a random wait time (0 to maxWaitTime)
        currentWaitTime = random.nextInt(0, maxWaitTime + 1)

        // Choose a new direction and speed
        angle = random.nextDouble(0.0, 360.0)
        speed = tripMinSpeed + random.nextDouble() * (tripMaxSpeed - tripMinSpeed)
    }
}
