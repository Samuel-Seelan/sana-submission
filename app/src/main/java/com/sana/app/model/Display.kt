package com.sana.app.model

/*
 * Display.kt — tiny formatting helpers shared by screens.
 * What: turns internal keys like "hip_flexors" into "Hip flexors" for display.
 * Who: Sana team (shared helper).
 * When: Goal 6 — UI skeleton.
 */
object Display {
    /** "hip_flexors" -> "Hip flexors". */
    fun muscleGroup(key: String): String =
        key.replace('_', ' ').replaceFirstChar { it.uppercase() }
}
