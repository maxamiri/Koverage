// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class RandomDirection(
    area: Area,
    private val tripMinSpeed: Double,
    private val tripMaxSpeed: Double,
    private val maxWaitTime: Int, // Maximum wait time
    random: Random
) : MobilityModel(area, random) {

    private var angle = random.nextDouble(0.0, 360.0)
    private var speed = tripMinSpeed + random.nextDouble() * (tripMaxSpeed - tripMinSpeed)
    private var currentWaitTime = 0

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