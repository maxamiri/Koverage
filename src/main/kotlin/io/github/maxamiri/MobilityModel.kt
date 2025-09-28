// Copyright (c) 2025 Max Amiri
// Licensed under the MIT License. See LICENSE file in the project root for full license information.

package io.github.maxamiri

import kotlin.random.Random

// Abstract MobilityModel class with a move function
abstract class MobilityModel(val area: Area, val random: Random) {
    abstract fun move(car: Car)
}
