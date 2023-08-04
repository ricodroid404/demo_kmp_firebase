package com.santansarah.kmmfirebasemessaging.android.services

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.santansarah.kmmfirebasemessaging.SharedRes
import com.santansarah.kmmfirebasemessaging.android.R
import com.santansarah.kmmfirebasemessaging.data.local.UserRepository
import com.santansarah.kmmfirebasemessaging.utils.SignUpHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import javax.inject.Inject
import kotlin.math.sign

class SignInService @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseAuth: FirebaseAuth,
    private val authUi: AuthUI
): KoinComponent {

    private val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.EmailBuilder().build()
    )

    val signInIntent = authUi
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .setLogo(SharedRes.images.account.drawableResId)
        .setTheme(R.style.SignInTheme)
        .build()

    private val SERVER_NONCE: String = SignUpHelper.getNonce()

    fun isUserSignedIn(scope: CoroutineScope) = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener {
            trySend(firebaseAuth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(), firebaseAuth.currentUser != null)

    suspend fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        Log.d("signin", "got here...${response?.error}")
        Log.d("signin", "got here...${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = firebaseAuth.currentUser
            Log.d("signin", user?.email.toString())

            user?.let {
                val token = firebaseMessaging.token.await()
                Log.d("signin", "token: $token")

                userRepository.upsertUser(it.uid, token)
            }
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    suspend fun signOut(activity: Context) {
        try {
            val success = authUi.signOut(activity).await()
        } catch (e: Exception) {
            Log.d("signin", "sign out error: ${e.message.toString()}")
        }
    }

}
