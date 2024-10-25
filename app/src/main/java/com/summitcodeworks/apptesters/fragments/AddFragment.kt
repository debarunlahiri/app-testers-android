package com.summitcodeworks.apptesters.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.summitcodeworks.apptesters.activities.RegisterActivity.Companion.TAG
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.databinding.FragmentAddBinding
import com.summitcodeworks.apptesters.models.UserAppRequest
import com.summitcodeworks.apptesters.models.responseHandler.ResponseHandler
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

        val storageRef: StorageReference = Firebase.storage("gs://app-testers-6b94a.appspot.com").reference


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
            val storageGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

            if (cameraGranted && storageGranted) {
                showImageSourceOptions()
            } else {
                Toast.makeText(mContext, "Permissions denied", Toast.LENGTH_SHORT).show()
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
            // Show a dialog to choose between camera or gallery
            checkPermissions()
        }

        viewBinding.bPostApp.setOnClickListener {
            val appName = viewBinding.tiePostAppName.text.toString()
            val appDescription = viewBinding.tiePostAppDescription.text.toString()
            val appLink = viewBinding.tiePostAppLink.text.toString()
            val appLogo = selectedImageUri
            val appDevName = viewBinding.tiePostAppDevName.text.toString()

            if (appName.isNotEmpty() && appDescription.isNotEmpty() && appLink.isNotEmpty() && appLogo != null && appDevName.isNotEmpty()) {
                // All fields are filled, proceed with uploading the app
                var userAppRequest = UserAppRequest()
                uploadImageToFirebase(appLogo, storageRef, userAppRequest)


            } else {
                // Show a message indicating that all fields are required
                Toast.makeText(mContext, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }

        }


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
                    Toast.makeText(mContext, "Image uploaded successfully: $downloadUrl", Toast.LENGTH_LONG).show()

                    userAppRequest.appName = viewBinding.tiePostAppName.text.toString()
                    userAppRequest.appDescription = viewBinding.tiePostAppDescription.text.toString()
                    userAppRequest.appLink = viewBinding.tiePostAppLink.text.toString()
                    userAppRequest.appLogo = downloadUrl.toString()

                    RetrofitClient.apiInterface.createApp(userAppRequest).enqueue(object : Callback<ResponseHandler> {
                        override fun onResponse(
                            p0: Call<ResponseHandler>,
                            p1: Response<ResponseHandler>
                        ) {

                        }

                        override fun onFailure(p0: Call<ResponseHandler>, p1: Throwable) {
                            Log.e(TAG, "Network request failed", p1)
                            if (p1 is IOException) {
                                Toast.makeText(mContext, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(mContext, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }

                    })
                }
            }
            .addOnFailureListener { e ->
                // Handle unsuccessful uploads
                Toast.makeText(mContext, "Image upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imageResultLauncher.launch(galleryIntent)
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