package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.databinding.ActivityRegisterBinding
import com.summitcodeworks.apptesters.models.UserRequest
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import jp.wasabeef.glide.transformations.BlurTransformation

class RegisterActivity : AppCompatActivity() {

    private lateinit var mContext: Context
    private lateinit var viewBinding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mContext = this

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        // Make the status bar transparent and text color white
        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = ViewCompat.getWindowInsetsController(window.decorView)
            controller?.isAppearanceLightStatusBars = false // White text and icons
        }

        Glide.with(this)
            .load(R.drawable.login_bg)
            .centerCrop()
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 5)))
            .into(viewBinding.imageView)

        viewBinding.bLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Set the click listener for the register button
        viewBinding.bRegister.setOnClickListener {
            val email = viewBinding.tieRegisterEmail.text.toString().trim()
            val password = viewBinding.tieRegisterPassword.text.toString()
            val name = viewBinding.tieRegisterName.text.toString().trim()

            if (isValidEmail(email) && isValidPassword(password) && name.isNotEmpty()) {
                registerWithEmail(email, password, name)
            } else {
                when {
                    !isValidEmail(email) -> Toast.makeText(mContext, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                    !isValidPassword(password) -> Toast.makeText(mContext, "Password must be at least 8 characters long, and include uppercase, lowercase, numbers, and special characters", Toast.LENGTH_LONG).show()
                    name.isEmpty() -> Toast.makeText(mContext, "Please enter your name", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Google Sign-In
        viewBinding.bGoogleSignUp.setOnClickListener {
            googleSignIn()
        }



        // Define the text
        val text = "By signing up, you are agreeing to our Terms and Conditions & Privacy Policy"

        // Create a SpannableString
        val spannableString = SpannableString(text)

        // "Terms and Conditions" ClickableSpan and UnderlineSpan
        val termsClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, WebViewActivity::class.java)
                intent.putExtra("webview_type", "terms")
                intent.putExtra("url", SharedPrefsManager.getTermsAndConditionsUrl(mContext))
                startActivity(intent)
            }
        }

        spannableString.setSpan(termsClickable, 39, 59, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), 39, 59, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(getColor(R.color.colorPrimary)), 39, 59, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // "Privacy Policy" ClickableSpan and UnderlineSpan
        val privacyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, WebViewActivity::class.java)
                intent.putExtra("webview_type", "policy")
                intent.putExtra("url", SharedPrefsManager.getAppPolicyUrl(mContext))
                startActivity(intent)
            }
        }

        spannableString.setSpan(privacyClickable, 62, 76, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), 62, 76, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(getColor(R.color.colorPrimary)), 62, 76, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the SpannableString on the TextView
        viewBinding.tvLegal.text = spannableString
        viewBinding.tvLegal.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }

    private fun registerWithEmail(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, save user details in REST API
                    val user = auth.currentUser
                    user?.let {
                        RetrofitClient.API_KEY = user.uid
                        val userRequest = UserRequest(user.uid, name, email)
                        saveUserDetails(userRequest)
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
                        RetrofitClient.API_KEY = user.uid
                        val userRequest = user?.let {
                            UserRequest(user.uid, user.displayName ?: "", user.email ?: "")
                        }
                        saveUserDetails(userRequest)
                    }
                } else {
                    Log.w(TAG, "Google sign in failed", task.exception)
                }
            }
    }

    private fun saveUserDetails(userRequest: UserRequest?) {



        if (userRequest != null) {
            // Access the apiInterface instance
            RetrofitClient.apiInterface(mContext).registerUser(userRequest).enqueue(object : retrofit2.Callback<ResponseHandler> {
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
                    CommonUtils.apiRequestFailedToast(mContext, t)
                }
            })
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Helper function to validate strong password
    private fun isValidPassword(password: String): Boolean {
        // Password should be at least 8 characters, contain an uppercase letter, a lowercase letter, a number, and a special character
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")
        return passwordPattern.matches(password)
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
