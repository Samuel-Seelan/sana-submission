package com.sana.app.vision

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

data class SquatRepResult(
    val reps: Int,
    val bodyDetected: Boolean,
    val status: String,
)

class SquatRepCounter {
    private enum class Phase { STANDING, LOWERED }

    private var phase = Phase.STANDING
    private var reps = 0

    fun update(pose: Pose): SquatRepResult {
        val angle = bestKneeAngle(pose)
            ?: return SquatRepResult(
                reps = reps,
                bodyDetected = false,
                status = "Move fully into frame",
            )

        when {
            phase == Phase.STANDING && angle < LOWERED_KNEE_ANGLE -> {
                phase = Phase.LOWERED
            }

            phase == Phase.LOWERED && angle > STANDING_KNEE_ANGLE -> {
                phase = Phase.STANDING
                reps += 1
            }
        }

        val status = when (phase) {
            Phase.STANDING -> "Stand tall, then squat"
            Phase.LOWERED -> "Drive back up"
        }
        return SquatRepResult(reps = reps, bodyDetected = true, status = status)
    }

    fun reset() {
        phase = Phase.STANDING
        reps = 0
    }

    private fun bestKneeAngle(pose: Pose): Double? {
        val left = kneeAngle(
            pose = pose,
            hipType = PoseLandmark.LEFT_HIP,
            kneeType = PoseLandmark.LEFT_KNEE,
            ankleType = PoseLandmark.LEFT_ANKLE,
        )
        val right = kneeAngle(
            pose = pose,
            hipType = PoseLandmark.RIGHT_HIP,
            kneeType = PoseLandmark.RIGHT_KNEE,
            ankleType = PoseLandmark.RIGHT_ANKLE,
        )
        return listOfNotNull(left, right).minOrNull()
    }

    private fun kneeAngle(
        pose: Pose,
        hipType: Int,
        kneeType: Int,
        ankleType: Int,
    ): Double? {
        val hip = pose.getPoseLandmark(hipType)?.takeIf { it.inFrameLikelihood > MIN_CONFIDENCE }
        val knee = pose.getPoseLandmark(kneeType)?.takeIf { it.inFrameLikelihood > MIN_CONFIDENCE }
        val ankle = pose.getPoseLandmark(ankleType)?.takeIf { it.inFrameLikelihood > MIN_CONFIDENCE }

        if (hip == null || knee == null || ankle == null) return null

        val hipPoint = hip.position
        val kneePoint = knee.position
        val anklePoint = ankle.position

        val firstVectorX = hipPoint.x - kneePoint.x
        val firstVectorY = hipPoint.y - kneePoint.y
        val secondVectorX = anklePoint.x - kneePoint.x
        val secondVectorY = anklePoint.y - kneePoint.y
        val dot = firstVectorX * secondVectorX + firstVectorY * secondVectorY
        val firstMagnitude = sqrt(firstVectorX.toDouble().pow(2) + firstVectorY.toDouble().pow(2))
        val secondMagnitude = sqrt(secondVectorX.toDouble().pow(2) + secondVectorY.toDouble().pow(2))
        if (firstMagnitude == 0.0 || secondMagnitude == 0.0) return null

        val cosine = (dot / (firstMagnitude * secondMagnitude)).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cosine))
    }

    private companion object {
        const val MIN_CONFIDENCE = 0.55f
        const val LOWERED_KNEE_ANGLE = 115.0
        const val STANDING_KNEE_ANGLE = 155.0
    }
}
