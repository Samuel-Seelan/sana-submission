package com.sana.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/*
 * FirestoreFlows.kt — bridges Firebase's callback listeners into Kotlin Flows.
 * What: wraps FirebaseAuth's auth-state listener and Firestore snapshot listeners as cold Flows so
 *       repositories can expose real-time data with viewModelScope-friendly coroutines. On error the
 *       flow closes; callers attach .catch { } to fall back to an empty/null value instead of crashing.
 * Who: Sana team (shared backend scaffold).
 * When: Goal 7 — Firebase integration.
 */

/** Emits the signed-in user's uid (or null when signed out), and again on every auth change. */
internal fun FirebaseAuth.uidFlow(): Flow<String?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

/** Emits the current document snapshot immediately, then again on every change. */
internal fun DocumentReference.snapshotFlow(): Flow<DocumentSnapshot?> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        trySend(snapshot)
    }
    awaitClose { registration.remove() }
}

/** Emits the current query results immediately, then again on every change. */
internal fun Query.snapshotFlow(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registration.remove() }
}
