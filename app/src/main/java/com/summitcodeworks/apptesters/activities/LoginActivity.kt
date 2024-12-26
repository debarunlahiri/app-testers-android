package com.summitcodeworks.apptesters.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
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
import com.summitcodeworks.apptesters.apiInterface.AppConstantsCallback
import com.summitcodeworks.apptesters.apiInterface.AuthenticationCallback
import com.summitcodeworks.apptesters.databinding.ActivityLoginBinding
import com.summitcodeworks.apptesters.models.UserRequest
import com.summitcodeworks.apptesters.models.appConstants.AppConstantsResponse
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
import com.summitcodeworks.apptesters.models.userDetails.UserDetails
import com.summitcodeworks.apptesters.models.userDetails.UserDetailsResponse
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import jp.wasabeef.glide.transformations.BlurTransformation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001 // Request code for Google sign-in

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mContext = this


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


        checkPermissions()
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
            RetrofitClient.apiInterface(mContext).registerUser(userRequest).enqueue(object : Callback<ResponseHandler> {
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
                        CommonUtils.authenticateUser(mContext, object : AuthenticationCallback {
                            override fun onSuccess(userDetails: UserDetailsResponse?) {
                                if (userDetails != null) {
                                    SharedPrefsManager.saveUserDetails(this@LoginActivity, userDetails)
                                    sendToMain()
                                } else {
                                    Log.e(TAG, "User details are null")
                                    Toast.makeText(this@LoginActivity, "User details are null", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onError(errorCode: Int, errorMessage: String) {
                            }

                            override fun onFailure(throwable: Throwable) {
                            }

                        })
                    } else {
                        Log.e(TAG, "Registration failed with code: ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseHandler>, t: Throwable) {
                    CommonUtils.apiRequestFailedToast(mContext, t)
                }
            })
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        // Check notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check file permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // Request permissions if not granted
        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray())
        } else {
            // All permissions are already granted
            // You can proceed with notifications and file access
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        // ActivityResultLauncher for permission request
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach { (permission, isGranted) ->
                if (isGranted) {
                    // Permission granted
                    // Handle permission granted logic
                } else {
                    // Permission denied
                    // Handle permission denied logic
                }
            }
        }

        // Launch the permission request
        requestPermissionLauncher.launch(permissions)
    }



    private fun sendToMain() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finishAffinity()
    }

    override fun onResume() {
        super.onResume()
        CommonUtils.showToastLong(mContext, "Checking server status")
        viewBinding.bLogin.isEnabled = false
        viewBinding.bGoogleSignIn.isEnabled = false
        viewBinding.bRegister.isEnabled = false
        CommonUtils.fetchAppConstant(mContext, packageName, object : AppConstantsCallback {
            override fun onSuccess(appConstantsResponseList: List<AppConstantsResponse>) {
                viewBinding.bLogin.isEnabled = true
                viewBinding.bGoogleSignIn.isEnabled = true
                viewBinding.bRegister.isEnabled = true
                for (appConstantsResponse in appConstantsResponseList)
                    when (appConstantsResponse.constantKey) {
                        "app_policy_url" ->  SharedPrefsManager.setAppPolicyUrl(mContext, appConstantsResponse.constantValue)
                        "terms_and_conditions_url" -> SharedPrefsManager.setTermsAndConditionsUrl(mContext, appConstantsResponse.constantValue)
                        "app_help_url" -> SharedPrefsManager.setSupportUrl(mContext, appConstantsResponse.constantValue)
                    }
            }

            override fun onError(errorCode: Int, errorMessage: String) {
            }

            override fun onFailure(throwable: Throwable) {
            }

        })
    }
}
