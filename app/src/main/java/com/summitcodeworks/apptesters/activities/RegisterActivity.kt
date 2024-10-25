package com.summitcodeworks.apptesters.activities

import android.content.Context
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.databinding.ActivityRegisterBinding
import com.summitcodeworks.apptesters.models.UserRequest
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
import jp.wasabeef.glide.transformations.BlurTransformation
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var mContext: Context
    private lateinit var viewBinding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Enable edge-to-edge display
        setContentView(viewBinding.root)

        mContext = this

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        RetrofitClient.initialize(this)

        // Make the status bar transparent and text color white
        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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

        // Set the click listener for the login button
        viewBinding.bLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Set the click listener for the register button
        viewBinding.bRegister.setOnClickListener {
            val email = viewBinding.tieRegisterEmail.text.toString()
            val password = viewBinding.tieRegisterPassword.text.toString()
            registerWithEmail(email, password)
        }

        // Google Sign-In
        viewBinding.bGoogleSignUp.setOnClickListener {
            googleSignIn()
        }
    }

    private fun registerWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, save user details in REST API
                    val user = auth.currentUser
                    user?.let {
                        saveUserDetails(user)
                    }
                } else {
                    Log.w(TAG, "Registration failed", task.exception)
                }
            }
    }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this string is set in strings.xml
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, authenticate with Firebase
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful, save user details in REST API
                    val user = auth.currentUser
                    user?.let {
                        saveUserDetails(user)
                    }
                } else {
                    Log.w(TAG, "Google sign in failed", task.exception)
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
            // Access the apiInterface instance
            RetrofitClient.apiInterface.registerUser(userRequest).enqueue(object : retrofit2.Callback<ResponseHandler> {
                override fun onResponse(
                    call: retrofit2.Call<ResponseHandler>,
                    response: retrofit2.Response<ResponseHandler>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "User registered successfully: ${response.body()}")
                        sendToMain()
                    } else {
                        Log.e(TAG, "Registration failed with code: ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<ResponseHandler>, t: Throwable) {
                    Log.e(TAG, "Network request failed", t)
                    if (t is IOException) {
                        Toast.makeText(this@RegisterActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RegisterActivity, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }



    private fun sendToMain() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finishAffinity()
    }

    companion object {
        const val TAG = "RegisterActivity"
        private const val RC_SIGN_IN = 9001
    }
}
