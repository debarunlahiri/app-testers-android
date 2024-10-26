package com.summitcodeworks.apptesters.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.activities.RegisterActivity.Companion.TAG
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.databinding.ActivityLoginBinding
import com.summitcodeworks.apptesters.models.UserRequest
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
import com.summitcodeworks.apptesters.models.userDetails.UserDetails
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import jp.wasabeef.glide.transformations.BlurTransformation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001 // Request code for Google sign-in

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Enable edge-to-edge display
        setContentView(viewBinding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Add your web client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Make the status bar transparent and text color white
        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        // For Android 11 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = ViewCompat.getWindowInsetsController(window.decorView)
            controller?.isAppearanceLightStatusBars = false // White text and icons
        }

        // Load the blurred image with Glide
        Glide.with(this)
            .load(R.drawable.login_bg) // Your drawable resource
            .centerCrop()
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 5)))
            .into(viewBinding.imageView)

        // Set the click listener for the register button
        viewBinding.bRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        // Set the click listener for the login button
        viewBinding.bLogin.setOnClickListener {
            loginWithEmailPassword()
        }

        // Set the click listener for the Google sign-in button
        viewBinding.bGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun loginWithEmailPassword() {
        val email = viewBinding.tieLoginEmail.text.toString().trim()
        val password = viewBinding.tieLoginPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        // Navigate to main activity
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // If sign in fails
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    // Sign in success
                    saveUserDetails(task.result?.user)
                    Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                    // Navigate to main activity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails
                    Toast.makeText(this, "Google Sign-In Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserDetails(user: FirebaseUser?) {
        if (user != null) {
            RetrofitClient.API_KEY = user.uid
        }
        val userRequest = user?.let {
            UserRequest(user.uid, user.displayName ?: "", user.email ?: "")
        }


        if (userRequest != null) {
            RetrofitClient.apiInterface.registerUser(userRequest).enqueue(object : Callback<ResponseHandler> {
                override fun onResponse(
                    call: Call<ResponseHandler>,
                    response: Response<ResponseHandler>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()?.header?.responseCode == 201) {
                            sendToMain()
                            Log.d(TAG, "User registered successfully: ${response.body()}")
                        }
                    } else if (response.code() == 409) {
                        authenticateUser()
                    } else {
                        Log.e(TAG, "Registration failed with code: ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseHandler>, t: Throwable) {
                    Log.e(TAG, "Network request failed", t)
                    if (t is IOException) {
                        Toast.makeText(this@LoginActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun authenticateUser() {
        RetrofitClient.apiInterface.authenticateUser().enqueue(object : Callback<UserDetails> {
            override fun onResponse(call: Call<UserDetails>, response: Response<UserDetails>) {
                if (response.isSuccessful) {
                    val userDetails = response.body()?.response
                    if (userDetails != null) {
                        SharedPrefsManager.saveUserDetails(this@LoginActivity, userDetails)
                        sendToMain()
                    } else {
                        Log.e(TAG, "User details are null")
                        Toast.makeText(this@LoginActivity, "User details are null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Login failed with code: ${response.code()} - ${response.message()}")
                    Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(p0: Call<UserDetails>, p1: Throwable) {
                Log.e(TAG, "Network request failed", p1)
                if (p1 is IOException) {
                    Toast.makeText(this@LoginActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun sendToMain() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finishAffinity()
    }
}
