package com.santansarah.kmmfirebasemessaging.data.local

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.icerock.moko.mvvm.flow.cFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.map

class UserRepository(
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
): ViewModel() {

    val isUserSignedIn = firebaseAuth.authStateChanged.map {
        it != null
    }.cFlow()

    suspend fun upsertUser(uid: String, token: String) {

        val user = hashMapOf(
            "uid" to uid,
            "token" to token
        )

        try {
            db.collection("users").document(uid)
                .set(user, merge = true)

        } catch (e: Exception) {
            Logger.d { e.message.toString() }
        }

    }

    suspend fun signOut() {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            Logger.d { e.message.toString() }
        }
    }

}