package com.sana.app.model

/*
 * Recommendations.kt — the injury-aware core of Sana.
 * What: maps a user's selected injuries to recommended and not-advised exercises, scores any
 *       exercise for the current injuries, and builds the curated starter plan saved at the end of
 *       onboarding. Used by onboarding (initial plan) and Edit playlist / Exercise detail (scoring).
 * Who: Sana team (shared domain logic).
 * When: Goal 7 — Firebase integration.
 */
object Recommendations {

    /** Exercises actively recommended for each injury, keyed by injuryId -> exerciseIds. */
    private val recommendedByInjury: Map<String, Set<String>> = mapOf(
        "acl_tear" to setOf("quad_sets", "straight_leg_raise", "heel_slides", "hamstring_stretch"),
        "meniscus_tear" to setOf("quad_sets", "straight_leg_raise", "wall_sit", "heel_slides"),
        "ankle_sprain" to setOf("single_leg_balance", "heel_slides", "hamstring_stretch"),
        "rotator_cuff" to setOf("pendulum_swings"),
        "lower_back_strain" to setOf("glute_bridge", "bird_dog", "hamstring_stretch"),
        "tennis_elbow" to setOf("pendulum_swings"),
    )

    /** Exercises to flag as not advised for each injury (demanding loaded movements early on). */
    private val blockedByInjury: Map<String, Set<String>> = mapOf(
        "acl_tear" to setOf("deadlift", "bodyweight_squat"),
        "meniscus_tear" to setOf("deadlift", "bodyweight_squat"),
        "ankle_sprain" to setOf("deadlift"),
        "rotator_cuff" to setOf("deadlift"),
        "lower_back_strain" to setOf("deadlift"),
        "tennis_elbow" to setOf("deadlift"),
    )

    fun recommendedExerciseIds(injuryIds: Set<String>): Set<String> =
        injuryIds.flatMap { recommendedByInjury[it].orEmpty() }.toSet()

    fun blockedExerciseIds(injuryIds: Set<String>): Set<String> =
        injuryIds.flatMap { blockedByInjury[it].orEmpty() }.toSet()

    /** Whether an exercise is recommended / blocked for the given injuries. Blocked wins. */
    fun scoreFor(exerciseId: String, injuryIds: Set<String>): ExerciseScore {
        val blocked = exerciseId in blockedExerciseIds(injuryIds)
        val recommended = !blocked && exerciseId in recommendedExerciseIds(injuryIds)
        return ExerciseScore(blocked = blocked, recommended = recommended)
    }

    /** The curated plan written to Firestore right after onboarding, built from the recommendations. */
    fun initialPlan(injuryIds: Set<String>): List<PlanItem> {
        val recommended = recommendedExerciseIds(injuryIds).mapNotNull { ExerciseCatalog.find(it) }
        val chosen = recommended.ifEmpty {
            // No injuries selected (or none mapped) — fall back to gentle, broadly safe defaults.
            listOfNotNull(
                ExerciseCatalog.find("quad_sets"),
                ExerciseCatalog.find("glute_bridge"),
                ExerciseCatalog.find("hamstring_stretch"),
            )
        }
        return chosen.take(6).map { exercise ->
            PlanItem(
                exercise = exercise,
                targetReps = exercise.defaultReps,
                targetSets = exercise.defaultSets,
                targetDurationSec = exercise.defaultDurationSec,
            )
        }
    }
}
