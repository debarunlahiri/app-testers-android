package com.summitcodeworks.apptesters.activities

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.adapter.CommunityAdapter
import com.summitcodeworks.apptesters.adapter.CommunityMediaAdapter
import com.summitcodeworks.apptesters.apiClient.WebSocketService
import com.summitcodeworks.apptesters.databinding.ActivityCommunityBinding
import com.summitcodeworks.apptesters.models.AppCommunity
import com.summitcodeworks.apptesters.models.ChatViewModel
import com.summitcodeworks.apptesters.models.CommunityMedia
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CommunityActivity : AppCompatActivity() {

    private val TAG: String? = CommunityActivity::class.simpleName
    private lateinit var viewBinding: ActivityCommunityBinding

    private lateinit var mContext: Context

    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private var chatViewModel = ChatViewModel()
    private lateinit var communityAdapter: CommunityAdapter
    private var communityList: MutableList<AppCommunity> = mutableListOf()
    private lateinit var communityMediaAdapter: CommunityMediaAdapter
    private var communityMediaList: MutableList<CommunityMedia> = mutableListOf()


    private var permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

        if (cameraGranted && storageGranted) {
            showImageSourceOptions()
        } else {
//                Toast.makeText(mContext, "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val imageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.data?.let {
                val media = CommunityMedia(it)
                communityMediaList.add(media)
                communityMediaAdapter.notifyDataSetChanged()
                if (communityMediaList.isNotEmpty()) {
                    viewBinding.rvCommunityMedia.visibility = View.VISIBLE
                } else {
                    viewBinding.rvCommunityMedia.visibility = View.GONE
                }
            }
        } else {
            Toast.makeText(mContext, "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mContext = this

        setupUI()

        viewBinding.cvChatAttachment.setOnClickListener {
            checkPermissions()
        }


        viewBinding.cvChatSendMessage.setOnClickListener {
            val message = viewBinding.etCommunityMessage.text.toString()
            if (message.isEmpty()) {
                CommonUtils.showToastLong(mContext, "Please enter a message.")
                return@setOnClickListener
            }
            if (communityMediaList.isNotEmpty()) {
                communityMediaList.forEach {
                    handleMedia(mContext, it.mediaUri)
                }.apply {
                    chatViewModel.sendMessage(viewBinding.etCommunityMessage.text.toString(), SharedPrefsManager.getUserDetails(mContext).userId, SharedPrefsManager.getUserDetails(mContext).userKey)
                    viewBinding.etCommunityMessage.text.clear()
                }
            } else {
                chatViewModel.sendMessage(viewBinding.etCommunityMessage.text.toString(), SharedPrefsManager.getUserDetails(mContext).userId, SharedPrefsManager.getUserDetails(mContext).userKey)
                viewBinding.etCommunityMessage.text.clear()
            }

        }

    }

    private fun setupRecyclerView() {
        communityAdapter = CommunityAdapter(mContext, communityList)
        viewBinding.rvCommunity.apply {
            adapter = communityAdapter
            layoutManager = LinearLayoutManager(mContext).apply {
                stackFromEnd = true
                reverseLayout = true
            }
            setupViewModel()
        }

        communityMediaAdapter = CommunityMediaAdapter(mContext, communityMediaList)
        viewBinding.rvCommunityMedia.apply {
            adapter = communityMediaAdapter
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        }

    }

    private fun setupViewModel() {
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.messages.collect { messages ->
                    Log.d("ChatActivity", "Updating UI with messages: ${messages.size}")
                    communityAdapter.submitList(messages)
                    viewBinding.rvCommunity.scrollToPosition(0)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.connectionStatus.collect { status ->
                    Log.d("ChatActivity", "Connection status changed: $status")
                    updateConnectionStatus(status)
                }
            }
        }
    }

    private fun updateConnectionStatus(status: WebSocketService.ConnectionStatus) {
        val (text, color) = when (status) {
            WebSocketService.ConnectionStatus.Connected -> "Connected" to Color.GREEN
            WebSocketService.ConnectionStatus.Disconnected -> "Disconnected" to Color.GRAY
            WebSocketService.ConnectionStatus.Error -> "Error" to Color.RED
        }
        viewBinding.tvCommunityConnectionStatus.apply {
            setTextColor(color)
            setText(text)
        }
    }

    private fun compressImage(uri: Uri): File? {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val tempFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.flush()
        outputStream.close()
        return tempFile
    }

    private fun compressVideo(uri: Uri): File? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val tempFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.mp4")
        val outputStream = FileOutputStream(tempFile)
        val buffer = ByteArray(1024)
        var bytesRead: Int

        inputStream?.use { input ->
            outputStream.use { output ->
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
            }
        }
        return tempFile
    }

    private fun saveMediaToFile(uri: Uri, type: String): File? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val tempFile = File(cacheDir, "temp_${System.currentTimeMillis()}.$type")
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        return tempFile
    }


    private fun handleMedia(context: Context, uri: Uri) {
        val type = getMediaType(context, uri)
        val tempFile = when (type) {
            "image" -> compressImage(uri)
            "video" -> compressVideo(uri)
            else -> saveMediaToFile(uri, type)
        }
        tempFile?.let {
            uploadToFirebase(it)
        }
    }

    private fun getMediaType(context: Context, uri: Uri): String {
        val contentResolver: ContentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: return "unknown"

        return when {
            mimeType.startsWith("image") -> "image"
            mimeType.startsWith("video") -> "video"
            else -> "unknown"
        }
    }

    private fun uploadToFirebase(file: File) {
        val fileUri = Uri.fromFile(file)
        val firebaseRef = storageReference.child("uploads/${file.name}")
        firebaseRef.putFile(fileUri)
            .addOnSuccessListener {
                firebaseRef.downloadUrl.addOnSuccessListener { uri ->
//                    CommonUtils.showToastLong(mContext, "Upload successful: $uri")

                }
            }
            .addOnFailureListener {
                CommonUtils.showToastLong(mContext, "Upload failed: ${it.message}")
            }
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

    private fun checkPermissions() {
        val cameraPermission = android.Manifest.permission.CAMERA
        val storagePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(mContext, cameraPermission) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(mContext, storagePermission) == PackageManager.PERMISSION_GRANTED) {
            showImageSourceOptions()
        } else {
            permissionLauncher.launch(arrayOf(cameraPermission, storagePermission))
        }
    }


    private fun setupUI() {
        viewBinding.pbCommunity.visibility = View.VISIBLE
        viewBinding.tbCommunity.title = "Community"

        setSupportActionBar(viewBinding.tbCommunity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewBinding.tbCommunity.setNavigationOnClickListener {
            finish()
        }

        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24)
        viewBinding.tbCommunity.setNavigationIcon(navigationIcon)

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                viewBinding.tbCommunity.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                viewBinding.tbCommunity.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
        }

        viewBinding.pbCommunity.visibility = View.GONE

        setupRecyclerView()
    }

    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbCommunity.navigationIcon = wrappedIcon
        }
    }

}