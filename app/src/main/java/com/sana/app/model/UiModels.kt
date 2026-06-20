package com.sana.app.model

/**
 * Plain UI models for the screen skeleton.
 *
 * The full app stores these in a Room database and derives them through repositories /
 * view models. For the UI-only milestone we keep simple, framework-free data classes so
 * every screen and @Preview can render with static dummy data and no backend.
 */

/** "strength" | "mobility" | "balance" | "stretch". */
object ExerciseType {
    const val STRENGTH = "strength"
    const val MOBILITY = "mobility"
    const val BALANCE = "balance"
    const val STRETCH = "stretch"
}

/** A catalog exercise (mirrors the catalog the real app seeds from assets). */
data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    /** Newline-separated numbered steps. */
    val instructions: String,
    /** "strength" | "mobility" | "balance" | "stretch". */
    val type: String,
    val muscleGroups: List<String>,
    /** 1 = gentle, 2 = moderate, 3 = demanding. */
    val difficulty: Int,
    val defaultReps: Int,
    val defaultSets: Int,
    /** For timed holds; 0 when rep-based. */
    val defaultDurationSec: Int,
    /** YouTube video id for the embedded exercise demonstration, if available. */
    val youtubeVideoId: String? = null,
) {
    /** "3 × 12 reps" or "3 × 30s" depending on whether the exercise is rep- or time-based. */
    fun targetLabel(): String =
        if (defaultDurationSec > 0) "$defaultSets × ${defaultDurationSec}s"
        else "$defaultSets × $defaultReps reps"
}

/** A hand-authored injury profile the user can select during onboarding. */
data class InjuryProfile(
    val id: String,
    val name: String,
    val description: String,
)

/** One entry in the user's plan: an exercise plus its per-user targets. */
data class PlanItem(
    val exercise: Exercise,
    val targetReps: Int,
    val targetSets: Int,
    val targetDurationSec: Int,
) {
    fun targetLabel(): String =
        if (targetDurationSec > 0) "$targetSets × ${targetDurationSec}s"
        else "$targetSets × $targetReps reps"
}

/** A finished exercise clip from a past session (drives Day detail / Exercise detail). */
data class Recording(
    val id: Long,
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val durationMs: Long,
    /** Null while the post-session splitter is still producing the clip. */
    val hasClip: Boolean,
)

/** One named marker on the weekly progress chart. */
data class Milestone(
    val id: Long,
    val label: String,
)

/** A bar/line point on the weekly progress chart. */
data class WeeklyStat(
    val weekStartEpochDay: Long,
    val totalReps: Int,
    val totalTimeMs: Long,
)

/** Aggregated stats for a single calendar day (Day detail stat chips). */
data class DayStats(
    val totalReps: Int,
    val totalSets: Int,
    val totalTimeMs: Long,
    val exercisesDone: Int,
)

/** Whether an exercise is recommended / blocked for the current user's injuries. */
data class ExerciseScore(
    val blocked: Boolean,
    val recommended: Boolean,
)

/** A catalog exercise paired with its score, used by Edit playlist / Exercise detail. */
data class ScoredExercise(
    val exercise: Exercise,
    val score: ExerciseScore,
)

/** One section of the catalog in Edit playlist (e.g. "Recommended", "Strength"). */
data class CatalogGroup(
    val key: String,
    val title: String,
    val exercises: List<ScoredExercise>,
)

// ---- Calendar / overview models ----

enum class DayStatus { DONE, MISSED, UPCOMING }

/** One calendar day cell (used by both the "This week" strip and the week-list dots). */
data class DayCell(
    val epochDay: Long,
    /** Narrow day-of-week letter, e.g. "M". */
    val dayLetter: String,
    val dayNumber: Int,
    val status: DayStatus,
    val isToday: Boolean,
)

/** One row in the week list, Monday-based. */
data class WeekRow(
    val weekStartEpochDay: Long,
    /** e.g. "Week of Jun 8". */
    val label: String,
    /** Exactly 7 cells, Monday..Sunday. */
    val days: List<DayCell>,
    val sessionCount: Int,
)

/** A user profile (Account / Home greeting). */
data class UserProfile(
    val name: String,
    val email: String,
)
