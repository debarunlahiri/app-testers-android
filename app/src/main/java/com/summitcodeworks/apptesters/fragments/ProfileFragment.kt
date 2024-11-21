package com.summitcodeworks.apptesters.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.activities.LoginActivity
import com.summitcodeworks.apptesters.activities.MyAppsActivity
import com.summitcodeworks.apptesters.activities.SettingsActivity
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.databinding.FragmentProfileBinding
import com.summitcodeworks.apptesters.models.appDetails.AppDetails
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import io.getstream.avatarview.coil.loadImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mContext: Context

    private lateinit var viewBinding: FragmentProfileBinding

    private lateinit var auth: FirebaseAuth


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
        viewBinding = FragmentProfileBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = requireContext()

        auth = FirebaseAuth.getInstance()

        fetchUserDetails()

        viewBinding.bLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(mContext, "Logged out successfully", Toast.LENGTH_SHORT).show()
            sentToLogin()
        }

        viewBinding.llYourApps.setOnClickListener {
            val myAppsIntent = Intent(mContext, MyAppsActivity::class.java)
            startActivity(myAppsIntent)
        }

        viewBinding.llSettings.setOnClickListener {
            val settingsIntent = Intent(mContext, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }

        viewBinding.llJoinGroup.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://groups.google.com/g/app-testers-community-summitcodeworks"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(mContext, "Unable to open URL", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun sentToLogin() {
        val loginIntent = Intent(mContext, LoginActivity::class.java)
        startActivity(loginIntent)
        requireActivity().finish()
    }


    private fun fetchUserDetails() {
        if (auth.currentUser != null) {
            viewBinding.tvProfileEmail.text = SharedPrefsManager.getUserDetails(mContext).userEmail
            viewBinding.tvProfileName.text = SharedPrefsManager.getUserDetails(mContext).userName
            if (SharedPrefsManager.getUserDetails(mContext).userPhotoUrl != null) {
                viewBinding.avProfileImage.loadImage(SharedPrefsManager.getUserDetails(mContext).userPhotoUrl)
            } else {
                viewBinding.avProfileImage.avatarInitials = SharedPrefsManager.getUserDetails(mContext).userName
            }
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}