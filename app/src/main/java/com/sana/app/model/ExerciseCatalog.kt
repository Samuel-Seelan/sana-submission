package com.sana.app.model

/*
 * ExerciseCatalog.kt — the app's read-only exercise + injury reference data.
 * What: a single lookup surface for the static catalog the app ships with. Exercises and injuries
 *       are stable reference data (not per-user), so they are bundled rather than fetched per user;
 *       only user data (profile, plan, sessions, shared playlists) lives in Firestore.
 * Who: Sana team (shared backend scaffold).
 * When: Goal 7 — Firebase integration.
 */
object ExerciseCatalog {
    /** Every catalog exercise, in a stable order. */
    val exercises: List<Exercise> = SampleData.exercises

    /** Every selectable injury profile. */
    val injuryProfiles: List<InjuryProfile> = SampleData.injuryProfiles

    /** Muscle-group keys used by the Edit playlist filter chips. */
    val muscleGroupKeys: List<String> = SampleData.muscleGroupKeys

    private val exercisesById: Map<String, Exercise> = exercises.associateBy { it.id }

    /** Resolve an exerciseId stored in Firestore back to its catalog [Exercise], or null if unknown. */
    fun find(exerciseId: String): Exercise? = exercisesById[exerciseId]
}
