package com.summitcodeworks.apptesters.activities

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.google.gson.Gson
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.adapter.CommunityAdapter
import com.summitcodeworks.apptesters.apiClient.WebSocketService
import com.summitcodeworks.apptesters.apiClient.WebSocketService.ConnectionStatus
import com.summitcodeworks.apptesters.databinding.ActivityCommunityBinding
import com.summitcodeworks.apptesters.models.AppCommunity
import com.summitcodeworks.apptesters.models.ChatViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                handleMedia(mContext, it)
            }
        }
    }

    private val captureVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val videoUri = result.data?.data
            videoUri?.let {
                handleMedia(mContext, it)
            }
        }
    }

    private val selectMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            handleMedia(mContext, it)
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
            checkPermissionsAndProceed()
        }


//        val message = it.chatMessage
//        val senderKey = it.senderKey
//        val chatTimestamp = it.chatTimestamp
//        val chatAttachment = it.chatAttachment
//        val senderId = it.senderId
//        val chatId = it.chatId
//        val useFlag = it.useFlag
//        val chat = AppCommunity(
//            chatId = chatId,
//            senderId = senderId,
//            senderKey = senderKey,
//            chatMessage = message,
//            chatAttachment = chatAttachment,
//            chatTimestamp = chatTimestamp,
//            useFlag = useFlag
//        )
//
//        Log.i(TAG, "onCreate:chatViewModel ${Gson().toJson(chat)}")

        viewBinding.cvChatSendMessage.setOnClickListener {
            val message = viewBinding.etCommunityMessage.text.toString()
            if (message.isEmpty()) {
                showToast("Please enter a message.")
                return@setOnClickListener
            }
            chatViewModel.sendMessage(viewBinding.etCommunityMessage.text.toString(), 1, "senderKey")
            viewBinding.etCommunityMessage.text.clear()
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

    private fun showMediaOptionsDialog() {
        val options = arrayOf("Capture Image", "Capture Video", "Select from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Choose Media Option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCameraForImage()
                    1 -> openCameraForVideo()
                    2 -> selectMediaFromGallery()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun openCameraForImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImage.launch(intent)
    }

    fun openCameraForVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        captureVideo.launch(intent)
    }

    fun selectMediaFromGallery() {
        selectMedia.launch("*/*")
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
                    showToast("Upload successful: $uri")
                }
            }
            .addOnFailureListener {
                showToast("Upload failed: ${it.message}")
            }
    }


    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showMediaOptionsDialog()
        } else {
            showToast("Permissions are required to proceed.")
        }
    }

    private fun checkPermissionsAndProceed() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.CAMERA)

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermission.launch(permissionsToRequest.toTypedArray())
        } else {
            showMediaOptionsDialog()
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