// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.random.Random

/**
 * Abstract base class for mobility models.
 *
 * Mobility models define how mobile sinks (cars) move through the simulation area.
 * Each implementation provides a specific movement pattern based on well-known
 * mobility models from the literature or custom synthetic models.
 *
 * @property area The simulation area boundaries that constrain movement.
 * @property random Random number generator for stochastic movement decisions.
 */
abstract class MobilityModel(val area: Area, val random: Random) {
    /**
     * Updates the position of a car based on the mobility model's movement logic.
     *
     * @param car The car to move. Its position will be updated by this method.
     */
    abstract fun move(car: Car)
}
