package com.summitcodeworks.apptesters.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.firebase.auth.FirebaseAuth
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.apiClient.GoogleRetrofitClient
import com.summitcodeworks.apptesters.apiInterface.GoogleApiInterface
import com.summitcodeworks.apptesters.databinding.FragmentGoogleGroupBinding
import com.summitcodeworks.apptesters.utils.CommonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GoogleGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GoogleGroupFragment : BottomSheetDialogFragment() {

    private val TAG: String? = GoogleGroupFragment::class.simpleName

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mContext: Context

    private lateinit var viewBinding: FragmentGoogleGroupBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gso: GoogleSignInOptions

    private val RC_SIGN_IN = 9001
    private val RC_AUTH_EXCEPTION = 9002
    private val GROUP_EMAIL = "app-testers-community-summitcodeworks@googlegroups.com"
    private val GROUP_URL = "https://groups.google.com/g/app-testers-community-summitcodeworks"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = FragmentGoogleGroupBinding.inflate(layoutInflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = requireContext()

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestScopes(Scope("https://www.googleapis.com/auth/admin.directory.group.member.readonly"))
            .requestScopes(Scope("https://www.googleapis.com/auth/admin.directory.group"))
            .requestScopes(Scope("https://www.googleapis.com/auth/groups.readonly"))
            .build()

        googleSignInClient = GoogleSignIn.getClient(mContext, gso)

        // Set up button click listeners
        viewBinding.signInButton.setOnClickListener { signIn() }
        viewBinding.checkMembershipButton.setOnClickListener { checkMembership() }
        viewBinding.joinGroupButton.setOnClickListener { joinGroup() }

        // Check if user is already signed in
        val account = GoogleSignIn.getLastSignedInAccount(mContext)
        updateUI(account)

    }

    private fun signIn() {
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
            updateUI(account)
        } catch (e: ApiException) {
            updateUI(null)
            Toast.makeText(mContext, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkMembership() {
        val account = GoogleSignIn.getLastSignedInAccount(mContext)
        if (account != null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // Get fresh access token using GoogleAccountCredential
                    val credential = GoogleAccountCredential.usingOAuth2(
                        mContext,
                        listOf("https://www.googleapis.com/auth/admin.directory.group.member.readonly")
                    )
                    credential.selectedAccount = account.account

                    // Since getToken() returns String directly, no need for Tasks.await
                    val accessToken = credential.getToken()

                    if (accessToken != null) {
                        checkGroupMembership(accessToken, GROUP_EMAIL, account.email!!)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                mContext,
                                "Failed to get access token",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: UserRecoverableAuthException) {
                    // Handle auth exception by starting the recovery intent
                    withContext(Dispatchers.Main) {
                        e.intent?.let { startActivityForResult(it, RC_AUTH_EXCEPTION) }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        handleApiError(e)
                    }
                }
            }
        }
    }


    private fun checkMembershipUsingAccessToken(account: GoogleSignInAccount) {
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get the token result
                val token = task.result?.token
                // Use the token here
                println("Access Token: $token")
                if (token != null) {
                    checkGroupMembership(token, GROUP_EMAIL, account.email!!)
                }
            } else {
                // Handle the error
                task.exception?.printStackTrace()
            }
        }
    }

    private fun checkGroupMembership(accessToken: String, groupEmail: String, userEmail: String) {
        val client = OkHttpClient.Builder()
            .addInterceptor(ChuckerInterceptor(mContext))
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val service = retrofit.create(GoogleApiInterface::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = service.checkMembership(
                    groupEmail = groupEmail,
                    userEmail = userEmail,
                )

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful -> {
                            val isMember = response.body()
                            viewBinding.statusText.text = if (isMember != null) {
                                "You are a member of the group"
                            } else {
                                "You are not a member of the group"
                            }
                            viewBinding.joinGroupButton.isEnabled = isMember == null
                        }

                        else -> {
                            Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                            handleApiError(HttpException(response))
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleApiError(e)
                }
            }
        }
    }


    private fun handleApiError(e: Exception) {
        when (e) {
            is HttpException -> {
                when (e.code()) {
                    403 -> Toast.makeText(mContext, "Access denied", Toast.LENGTH_SHORT).show()
                    404 -> Toast.makeText(mContext, "Group or member not found", Toast.LENGTH_SHORT)
                        .show()

                    else -> Toast.makeText(mContext, "Unknown error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            else -> {
                Toast.makeText(mContext, "An error occurred: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun joinGroup() {
        // Open the Google Groups page in a browser
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(GROUP_URL)
        startActivity(intent)
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            viewBinding.statusText.text = "Signed in as: ${account.email}"
            viewBinding.signInButton.text = "Sign Out"
            viewBinding.signInButton.setOnClickListener { signOut() }
            viewBinding.checkMembershipButton.isEnabled = true
        } else {
            viewBinding.statusText.text = "Not signed in"
            viewBinding.signInButton.text = "Sign In with Google"
            viewBinding.signInButton.setOnClickListener { signIn() }
            viewBinding.checkMembershipButton.isEnabled = false
            viewBinding.joinGroupButton.isEnabled = false
        }
    }

    private fun signOut() {
//        googleSignInClient.signOut().addOnCompleteListener(mContext) {
//            updateUI(null)
//        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GoogleGroupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GoogleGroupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    data class MembershipResponse(
        val status: String
    )
}

