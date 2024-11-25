package com.summitcodeworks.apptesters.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.activities.HelpActivity
import com.summitcodeworks.apptesters.activities.RegisterActivity.Companion.TAG
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.apiInterface.AuthenticationCallback
import com.summitcodeworks.apptesters.databinding.FragmentAddBinding
import com.summitcodeworks.apptesters.models.UserAppRequest
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
import com.summitcodeworks.apptesters.models.userDetails.UserDetails
import com.summitcodeworks.apptesters.models.userDetails.UserDetailsResponse
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.CommonUtils.Companion.applyBoldStyle
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mContext: Context

    private lateinit var viewBinding: FragmentAddBinding

    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

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
        viewBinding = FragmentAddBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = requireContext()

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        viewBinding.pbPostAppProgressBar.visibility = View.GONE
        getUserCredits()

        val storageRef: StorageReference = Firebase.storage("gs://app-testers-6b94a.appspot.com").reference

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
            val storageGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

            if (cameraGranted && storageGranted) {
                showImageSourceOptions()
            } else {
//                Toast.makeText(mContext, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }


        imageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                selectedImageUri = data?.data
                selectedImageUri?.let { uri ->
                    // You can use this URI to display the image in an ImageView or upload it
                    viewBinding.ivUploadAppLogo.setImageURI(uri)
                }
            } else {
                Toast.makeText(mContext, "Image selection cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        viewBinding.bUploadLogo.setOnClickListener {
            checkPermissions()
        }

        viewBinding.bPostApp.setOnClickListener {
            if (currentUser.isEmailVerified) {
                viewBinding.pbPostAppProgressBar.visibility = View.VISIBLE
                viewBinding.bPostApp.visibility = View.GONE

                val appName = viewBinding.tiePostAppName.text.toString()
                val appDescription = viewBinding.tiePostAppDescription.text.toString()
                val appLink = viewBinding.tiePostAppLink.text.toString()
                val appLogo = selectedImageUri
                val appDevName = viewBinding.tiePostAppDevName.text.toString()

                if (appName.isNotEmpty() && appDescription.isNotEmpty() && appLink.isNotEmpty() && appLogo != null && appDevName.isNotEmpty()) {
                    if (Patterns.WEB_URL.matcher(appLink).matches() && appLink.contains("id=") && appLink.contains("com.")) {
                        val userAppRequest = UserAppRequest()
                        uploadImageToFirebase(appLogo, storageRef, userAppRequest)
                    } else {
                        Toast.makeText(mContext, "Please enter a valid URL with an Android package name", Toast.LENGTH_SHORT).show()
                        viewBinding.pbPostAppProgressBar.visibility = View.GONE
                        viewBinding.bPostApp.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(mContext, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    viewBinding.pbPostAppProgressBar.visibility = View.GONE
                    viewBinding.bPostApp.visibility = View.VISIBLE
                }
            } else {
                CommonUtils.showToastLong(mContext, "Please verify your e-mail first")
            }

        }

        viewBinding.tvPostAppRefresh.setOnClickListener {
            getUserCredits()
        }

        viewBinding.cvAddHelp.setOnClickListener {
            val helpIntent = Intent(mContext, HelpActivity::class.java)
            helpIntent.putExtra("support_url", SharedPrefsManager.getSupportUrl(mContext))
            startActivity(helpIntent)
        }


        viewBinding.tiePostAppDevName.setText(SharedPrefsManager.getUserDetails(mContext).userName)
        viewBinding.tiePostAppDevName.isEnabled = false
    }

    private fun getUserCredits() {
        CommonUtils.authenticateUser(mContext, object : AuthenticationCallback {
            override fun onSuccess(userDetails: UserDetailsResponse?) {
                if (userDetails != null) {
                    SharedPrefsManager.saveUserDetails(requireContext(), userDetails)
                    val postedOnText = "Available Credits: ${userDetails.userCredits}/60"
                    viewBinding.tvPostAppAvailableCredits.text = applyBoldStyle("Available Credits:", postedOnText)

                    if (userDetails.userCredits >= 60) {
                        viewBinding.bPostApp.isEnabled = true
                    } else {
                        viewBinding.bPostApp.isEnabled = false
                    }
                } else {
                    Log.e(TAG, "User details are null")
                    Toast.makeText(requireContext(), "User details are null", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(errorCode: Int, errorMessage: String) {
            }

            override fun onFailure(throwable: Throwable) {
            }

        })
    }

    private fun uploadImageToFirebase(
        fileUri: Uri,
        storageRef: StorageReference,
        userAppRequest: UserAppRequest
    ) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val fileRef = storageRef.child("app_logos/$fileName")

        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                // Get the download URL
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Handle successful image upload (e.g., save the URL to your database)
                    userAppRequest.appName = viewBinding.tiePostAppName.text.toString()
                    userAppRequest.appDescription = viewBinding.tiePostAppDescription.text.toString()
                    userAppRequest.appLink = viewBinding.tiePostAppLink.text.toString()
                    userAppRequest.appWebLink = viewBinding.tiePostAppWebLink.text.toString()
                    userAppRequest.appLogo = downloadUrl.toString()
                    userAppRequest.userCreatedBy = SharedPrefsManager.getUserDetails(mContext).userId.toString()
                    userAppRequest.appDevName = viewBinding.tiePostAppDevName.text.toString()
                    userAppRequest.appCredit = "20"

                    RetrofitClient.apiInterface(mContext).createApp(userAppRequest).enqueue(object : Callback<ResponseHandler> {
                        override fun onResponse(
                            call: Call<ResponseHandler>,
                            response: Response<ResponseHandler>
                        ) {
                            if (response.isSuccessful) {
                                if (response.code() == 201) {
                                    Toast.makeText(mContext, "App created successfully", Toast.LENGTH_SHORT).show()
                                    viewBinding.pbPostAppProgressBar.visibility = View.GONE
                                    viewBinding.bPostApp.visibility = View.VISIBLE
                                    viewBinding.bPostApp.isEnabled = true
                                    clearFields()
                                    CommonUtils.authenticateUser(mContext)
                                }
                            } else {
                                viewBinding.pbPostAppProgressBar.visibility = View.GONE
                                viewBinding.bPostApp.visibility = View.VISIBLE
                                Toast.makeText(mContext, "Error: " + (response.body()?.header?.responseMessage
                                    ?: ""), Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(p0: Call<ResponseHandler>, p1: Throwable) {
                            viewBinding.pbPostAppProgressBar.visibility = View.GONE
                            viewBinding.bPostApp.visibility = View.VISIBLE
                            CommonUtils.apiRequestFailedToast(mContext, p1)
                        }

                    })
                }
            }
            .addOnFailureListener { e ->
                // Handle unsuccessful uploads
                Toast.makeText(mContext, "Image upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                viewBinding.pbPostAppProgressBar.visibility = View.GONE
                viewBinding.bPostApp.visibility = View.VISIBLE
            }
    }

    private fun clearFields() {
        viewBinding.tiePostAppWebLink.text?.clear()
        viewBinding.tiePostAppName.text?.clear()
        viewBinding.tiePostAppDescription.text?.clear()
        viewBinding.tiePostAppLink.text?.clear()
        viewBinding.tiePostAppDevName.text?.clear()
        Glide.with(mContext).load(R.drawable.ic_launcher_foreground).into(viewBinding.ivUploadAppLogo)
    }

    private fun checkPermissions() {
        val cameraPermission = android.Manifest.permission.CAMERA
        val storagePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // Check if both permissions are already granted
        if (ContextCompat.checkSelfPermission(mContext, cameraPermission) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(mContext, storagePermission) == PackageManager.PERMISSION_GRANTED) {
            showImageSourceOptions()
        } else {
            // Request camera and storage/media permissions
            permissionLauncher.launch(arrayOf(cameraPermission, storagePermission))
        }
    }


    private fun showImageSourceOptions() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(mContext)
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageResultLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val fileManagerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"  // restrict to image files only
        }
        imageResultLauncher.launch(fileManagerIntent)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}