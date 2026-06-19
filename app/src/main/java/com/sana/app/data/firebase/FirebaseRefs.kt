package com.sana.app.data.firebase

import com.google.firebase.firestore.FirebaseFirestore

/*
 * FirebaseRefs.kt — every Firestore path in one place.
 * What: centralizes collection/document references so path strings live in exactly one file.
 * Who: Sana team (shared backend scaffold).
 * When: Goal 7 — Firebase integration.
 *
 * Layout:
 *   users/{uid}/profile/private
 *   users/{uid}/plan/{exerciseId}
 *   users/{uid}/sessions/{sessionId}
 *   users/{uid}/recordings/{recordingId}     (flat, for easy day/exercise queries)
 *   sharedPlaylists/{playlistId}/items/{exerciseId}
 */
class FirebaseRefs(private val db: FirebaseFirestore) {

    val sharedPlaylists = db.collection("sharedPlaylists")

    private fun user(uid: String) = db.collection("users").document(uid)

    fun userProfile(uid: String) = user(uid).collection("profile").document("private")

    fun userPlan(uid: String) = user(uid).collection("plan")

    fun userSessions(uid: String) = user(uid).collection("sessions")

    fun userRecordings(uid: String) = user(uid).collection("recordings")

    fun sharedPlaylistItems(playlistId: String) =
        sharedPlaylists.document(playlistId).collection("items")
}
