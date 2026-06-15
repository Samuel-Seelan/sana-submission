package com.sana.app.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Central source of dummy data for @Preview functions and the runnable skeleton.
 *
 * Values are representative of the real seed catalog (knee / shoulder / back rehab
 * exercises and injuries) so previews look like the finished app. None of this is
 * persisted — it is recreated in-memory every time.
 */
object SampleData {

    // ---- Exercises (a representative slice of the real catalog) ----

    val quadSets = Exercise(
        id = "quad_sets",
        name = "Quad Sets",
        description = "A gentle isometric that wakes up the quadriceps without moving the knee — " +
            "ideal early in knee recovery.",
        instructions = "1. Sit with your leg straight out in front of you.\n" +
            "2. Tighten the muscle on top of your thigh, pressing the back of your knee down.\n" +
            "3. Hold for 5 seconds, then relax.\n" +
            "4. Repeat for the prescribed reps.",
        type = ExerciseType.STRENGTH,
        muscleGroups = listOf("quadriceps"),
        difficulty = 1,
        defaultReps = 10,
        defaultSets = 3,
        defaultDurationSec = 0,
    )

    val straightLegRaise = Exercise(
        id = "straight_leg_raise",
        name = "Straight Leg Raise",
        description = "Builds quad and hip-flexor strength while keeping the knee fully supported.",
        instructions = "1. Lie on your back, one knee bent and the other leg straight.\n" +
            "2. Tighten your thigh and lift the straight leg to the height of the bent knee.\n" +
            "3. Lower slowly with control.\n" +
            "4. Repeat for the prescribed reps.",
        type = ExerciseType.STRENGTH,
        muscleGroups = listOf("quadriceps", "hip_flexors"),
        difficulty = 1,
        defaultReps = 10,
        defaultSets = 3,
        defaultDurationSec = 0,
    )

    val heelSlides = Exercise(
        id = "heel_slides",
        name = "Heel Slides",
        description = "Restores knee bend by sliding the heel toward you through a comfortable range.",
        instructions = "1. Lie on your back with both legs straight.\n" +
            "2. Slowly slide one heel toward your buttocks, bending the knee.\n" +
            "3. Hold briefly, then slide back out.\n" +
            "4. Repeat for the prescribed reps.",
        type = ExerciseType.MOBILITY,
        muscleGroups = listOf("hamstrings", "quadriceps"),
        difficulty = 1,
        defaultReps = 10,
        defaultSets = 3,
        defaultDurationSec = 0,
    )

    val wallSit = Exercise(
        id = "wall_sit",
        name = "Wall Sit",
        description = "An isometric hold that builds quad endurance against a wall for support.",
        instructions = "1. Stand with your back flat against a wall.\n" +
            "2. Slide down until your knees are bent toward 90 degrees.\n" +
            "3. Hold for the prescribed time, keeping knees behind your toes.\n" +
            "4. Slide back up to rest.",
        type = ExerciseType.STRENGTH,
        muscleGroups = listOf("quadriceps", "glutes"),
        difficulty = 2,
        defaultReps = 0,
        defaultSets = 3,
        defaultDurationSec = 30,
    )

    val pendulumSwings = Exercise(
        id = "pendulum_swings",
        name = "Pendulum Swings",
        description = "Gentle, passive shoulder motion that eases early rotator-cuff stiffness.",
        instructions = "1. Lean forward and support yourself with your good arm.\n" +
            "2. Let the injured arm hang relaxed.\n" +
            "3. Gently swing it in small circles, letting momentum do the work.\n" +
            "4. Switch directions halfway through.",
        type = ExerciseType.MOBILITY,
        muscleGroups = listOf("shoulders"),
        difficulty = 1,
        defaultReps = 10,
        defaultSets = 2,
        defaultDurationSec = 0,
    )

    val birdDog = Exercise(
        id = "bird_dog",
        name = "Bird Dog",
        description = "A core-stability classic that trains the back and abs to work together.",
        instructions = "1. Start on your hands and knees.\n" +
            "2. Extend your opposite arm and leg until level with your back.\n" +
            "3. Hold briefly, keeping your hips square.\n" +
            "4. Return and switch sides.",
        type = ExerciseType.BALANCE,
        muscleGroups = listOf("core", "glutes", "back"),
        difficulty = 2,
        defaultReps = 8,
        defaultSets = 3,
        defaultDurationSec = 0,
    )

    val hamstringStretch = Exercise(
        id = "hamstring_stretch",
        name = "Hamstring Stretch",
        description = "Lengthens tight hamstrings to take strain off the knee and lower back.",
        instructions = "1. Sit with one leg straight and the other bent.\n" +
            "2. Reach gently toward the toes of the straight leg.\n" +
            "3. Hold the stretch without bouncing.\n" +
            "4. Ease off slowly.",
        type = ExerciseType.STRETCH,
        muscleGroups = listOf("hamstrings"),
        difficulty = 1,
        defaultReps = 0,
        defaultSets = 3,
        defaultDurationSec = 30,
    )

    val deadlift = Exercise(
        id = "deadlift",
        name = "Deadlift",
        description = "A demanding full-body lift — usually reserved for the late stage of recovery.",
        instructions = "1. Stand with feet hip-width, bar over mid-foot.\n" +
            "2. Hinge at the hips and grip the bar.\n" +
            "3. Drive through the floor to stand tall.\n" +
            "4. Lower with control, keeping your back neutral.",
        type = ExerciseType.STRENGTH,
        muscleGroups = listOf("hamstrings", "glutes", "back"),
        difficulty = 3,
        defaultReps = 8,
        defaultSets = 3,
        defaultDurationSec = 0,
    )

    val singleLegBalance = Exercise(
        id = "single_leg_balance",
        name = "Single Leg Balance",
        description = "Rebuilds the ankle and knee proprioception that prevents re-injury.",
        instructions = "1. Stand near a counter for support.\n" +
            "2. Lift one foot off the floor.\n" +
            "3. Balance for the prescribed time.\n" +
            "4. Switch legs.",
        type = ExerciseType.BALANCE,
        muscleGroups = listOf("ankles", "calves", "core"),
        difficulty = 2,
        defaultReps = 0,
        defaultSets = 3,
        defaultDurationSec = 20,
    )

    val gluteBridge = Exercise(
        id = "glute_bridge",
        name = "Glute Bridge",
        description = "Strengthens the glutes and core to offload a strained lower back.",
        instructions = "1. Lie on your back, knees bent, feet flat.\n" +
            "2. Squeeze your glutes and lift your hips into a straight line.\n" +
            "3. Hold briefly at the top.\n" +
            "4. Lower with control.",
        type = ExerciseType.STRENGTH,
        muscleGroups = listOf("glutes", "core"),
        difficulty = 1,
        defaultReps = 12,
        defaultSets = 3,
        defaultDurationSec = 0,
    )

    /** A broad catalog slice for grids and carousels. */
    val exercises: List<Exercise> = listOf(
        quadSets, straightLegRaise, heelSlides, wallSit, pendulumSwings,
        birdDog, hamstringStretch, deadlift, singleLegBalance, gluteBridge,
    )

    // ---- Injuries ----

    val injuryProfiles: List<InjuryProfile> = listOf(
        InjuryProfile(
            id = "acl_tear",
            name = "ACL Tear",
            description = "Injury to the anterior cruciate ligament of the knee. Recovery focuses on " +
                "restoring quad strength, knee range of motion, and balance.",
        ),
        InjuryProfile(
            id = "meniscus_tear",
            name = "Meniscus Tear",
            description = "Tear of the knee's shock-absorbing cartilage. Rehab builds the muscles " +
                "around the knee while avoiding deep flexion under load.",
        ),
        InjuryProfile(
            id = "ankle_sprain",
            name = "Ankle Sprain",
            description = "Overstretched or torn ankle ligaments. Recovery restores range of motion, " +
                "then strength, then balance.",
        ),
        InjuryProfile(
            id = "rotator_cuff",
            name = "Rotator Cuff Injury",
            description = "Strain or tear of the shoulder's stabilizing muscles. Rehab starts with " +
                "gentle motion, then isolated strengthening.",
        ),
        InjuryProfile(
            id = "lower_back_strain",
            name = "Lower Back Strain",
            description = "Strained muscles or ligaments in the lumbar spine. Recovery centers on " +
                "gentle mobility and core stabilization.",
        ),
        InjuryProfile(
            id = "tennis_elbow",
            name = "Tennis Elbow",
            description = "Overuse injury of the forearm extensor tendons. Rehab combines stretching " +
                "with slow eccentric loading.",
        ),
    )

    // ---- Plan ----

    val planItems: List<PlanItem> = listOf(
        PlanItem(quadSets, targetReps = 12, targetSets = 3, targetDurationSec = 0),
        PlanItem(straightLegRaise, targetReps = 10, targetSets = 3, targetDurationSec = 0),
        PlanItem(wallSit, targetReps = 0, targetSets = 3, targetDurationSec = 30),
        PlanItem(heelSlides, targetReps = 10, targetSets = 3, targetDurationSec = 0),
        PlanItem(hamstringStretch, targetReps = 0, targetSets = 3, targetDurationSec = 30),
    )

    val planExercises: List<Exercise> = planItems.map { it.exercise }

    // ---- Catalog (scored) for Edit playlist ----

    val scoredCatalog: List<ScoredExercise> = listOf(
        ScoredExercise(quadSets, ExerciseScore(blocked = false, recommended = true)),
        ScoredExercise(straightLegRaise, ExerciseScore(blocked = false, recommended = true)),
        ScoredExercise(heelSlides, ExerciseScore(blocked = false, recommended = true)),
        ScoredExercise(wallSit, ExerciseScore(blocked = false, recommended = false)),
        ScoredExercise(gluteBridge, ExerciseScore(blocked = false, recommended = false)),
        ScoredExercise(singleLegBalance, ExerciseScore(blocked = false, recommended = false)),
        ScoredExercise(deadlift, ExerciseScore(blocked = true, recommended = false)),
    )

    val catalogGroups: List<CatalogGroup> = listOf(
        CatalogGroup(
            key = "recommended",
            title = "Recommended",
            exercises = scoredCatalog.filter { it.score.recommended },
        ),
        CatalogGroup(
            key = "strength",
            title = "Strength",
            exercises = scoredCatalog.filter { it.exercise.type == ExerciseType.STRENGTH },
        ),
        CatalogGroup(
            key = "mobility",
            title = "Mobility & balance",
            exercises = scoredCatalog.filter {
                it.exercise.type == ExerciseType.MOBILITY || it.exercise.type == ExerciseType.BALANCE
            },
        ),
    )

    val muscleGroupKeys: List<String> =
        listOf("quadriceps", "hamstrings", "glutes", "core", "shoulders", "ankles")

    // ---- Recordings ----

    val recordings: List<Recording> = listOf(
        Recording(1, quadSets, sets = 3, reps = 12, durationMs = 95_000, hasClip = true),
        Recording(2, straightLegRaise, sets = 3, reps = 10, durationMs = 88_000, hasClip = true),
        Recording(3, wallSit, sets = 3, reps = 0, durationMs = 120_000, hasClip = true),
        Recording(4, heelSlides, sets = 2, reps = 10, durationMs = 70_000, hasClip = false),
    )

    // ---- Progress chart ----

    /** Eight weeks of upward-trending reps/time, anchored to recent Mondays. */
    val weeklyStats: List<WeeklyStat> = run {
        val thisMonday = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val reps = listOf(60, 78, 72, 110, 130, 124, 165, 190)
        val minutes = listOf(8, 12, 11, 16, 19, 18, 24, 27)
        reps.indices.map { i ->
            val weekStart = thisMonday.minusWeeks((reps.size - 1 - i).toLong())
            WeeklyStat(
                weekStartEpochDay = weekStart.toEpochDay(),
                totalReps = reps[i],
                totalTimeMs = minutes[i] * 60_000L,
            )
        }
    }

    val milestones: List<Milestone> = listOf(
        Milestone(1, "First session"),
        Milestone(2, "100 reps"),
        Milestone(3, "10 sessions"),
    )

    val dayStats = DayStats(
        totalReps = 64,
        totalSets = 12,
        totalTimeMs = 18 * 60_000L,
        exercisesDone = 5,
    )

    // ---- Overview calendar ----

    private fun dayLetter(date: LocalDate): String = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "M"
        DayOfWeek.TUESDAY -> "T"
        DayOfWeek.WEDNESDAY -> "W"
        DayOfWeek.THURSDAY -> "T"
        DayOfWeek.FRIDAY -> "F"
        DayOfWeek.SATURDAY -> "S"
        DayOfWeek.SUNDAY -> "S"
    }

    /** Builds a Monday..Sunday strip for [weekStart], assigning a plausible status to each day. */
    private fun buildWeek(weekStart: LocalDate, today: LocalDate): List<DayCell> =
        (0..6).map { offset ->
            val date = weekStart.plusDays(offset.toLong())
            val status = when {
                date.isAfter(today) -> DayStatus.UPCOMING
                // Simple deterministic pattern so previews look lived-in.
                date.dayOfWeek == DayOfWeek.SUNDAY || date.dayOfWeek == DayOfWeek.WEDNESDAY ->
                    DayStatus.MISSED
                else -> DayStatus.DONE
            }
            DayCell(
                epochDay = date.toEpochDay(),
                dayLetter = dayLetter(date),
                dayNumber = date.dayOfMonth,
                status = status,
                isToday = date == today,
            )
        }

    val thisWeek: List<DayCell> = run {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        buildWeek(weekStart, today)
    }

    val weeks: List<WeekRow> = run {
        val today = LocalDate.now()
        val thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        (0..3).map { back ->
            val weekStart = thisMonday.minusWeeks(back.toLong())
            val days = buildWeek(weekStart, today)
            WeekRow(
                weekStartEpochDay = weekStart.toEpochDay(),
                label = "Week of ${weekStart.month.name.lowercase()
                    .replaceFirstChar { it.uppercase() }} ${weekStart.dayOfMonth}",
                days = days,
                sessionCount = days.count { it.status == DayStatus.DONE },
            )
        }
    }

    val user = UserProfile(name = "Alex Rivera", email = "alex.rivera@example.com")

    val selectedInjuryIds: Set<String> = setOf("acl_tear", "lower_back_strain")
}
