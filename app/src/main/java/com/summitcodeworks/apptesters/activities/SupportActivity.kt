package com.summitcodeworks.apptesters.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.summitcodeworks.apptesters.R
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.apiInterface.AuthenticationCallback
import com.summitcodeworks.apptesters.databinding.ActivitySupportBinding
import com.summitcodeworks.apptesters.models.ChatMessage
import com.summitcodeworks.apptesters.models.Role
import com.summitcodeworks.apptesters.models.userDetails.UserDetailsResponse
import com.summitcodeworks.apptesters.utils.CommonUtils
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class SupportActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private lateinit var viewBinding: ActivitySupportBinding

    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mContext = this

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        setupUI()

        CommonUtils.authenticateUser(mContext, object : AuthenticationCallback {
            override fun onSuccess(userDetails: UserDetailsResponse?) {
                if (userDetails != null) {
                    viewBinding.tieSupportDeveloperName.setText(userDetails.userName)
                    viewBinding.tieSupportDeveloperEmail.setText(userDetails.userEmail)
                } else {
                    CommonUtils.showToastLong(mContext, "User details are null")
                }
            }

            override fun onError(errorCode: Int, errorMessage: String) {
            }

            override fun onFailure(throwable: Throwable) {
            }
        })

        // Launch file picker to select an image or video
        val selectFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Handle the selected file and send email
                val file = copyUriToFile(uri)
                if (file != null) {
                    this.file = file
                    viewBinding.ivScreenshot.visibility = View.VISIBLE
                    viewBinding.cvScreenshot.visibility = View.VISIBLE
                    Glide.with(mContext).load(uri).into(viewBinding.ivScreenshot)
                    viewBinding.bAttachScreenshot.text = "Change Screenshot"
                } else {
                    CommonUtils.showToastLong(mContext, "Failed to attach file")
                }
            }
        }

        viewBinding.bAttachScreenshot.setOnClickListener {
            selectFileLauncher.launch("image/*, video/*")
        }

        viewBinding.bStartChat.setOnClickListener {
            showProgressBar()
            val message = viewBinding.tieSupportProblem.text.toString()
            val developerName = viewBinding.tieSupportDeveloperName.text.toString()
            val developerEmail = viewBinding.tieSupportDeveloperEmail.text.toString()
            if (message.isNotEmpty() && developerName.isNotEmpty() && developerEmail.isNotEmpty()) {
                if (::file.isInitialized && file != null) {
                    sendEmailWithAttachment(
                        recipient = "summitcodeworks@gmail.com",
                        subject = "App Testers Bug Report",
                        body = message,
                        attachment = file
                    )
                } else {
                    sendEmail(
                        recipient = "summitcodeworks@gmail.com",
                        subject = "App Testers Bug Report",
                        body = message
                    )
                }
            } else {
                CommonUtils.showToastLong(mContext, "Please fill all the fields")
                hideProgressBar()
            }
        }
    }

    private fun copyUriToFile(uri: Uri): File? {
        return try {
            val fileName = getFileName(uri) ?: "temp_file"
            val tempFile = File(filesDir, fileName)
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun sendEmail(
        recipient: String,
        subject: String,
        body: String
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, "Choose an Email client"))
            hideProgressBar()
        } else {
            println("No email apps available!")
        }
    }

    private fun sendEmailWithAttachment(
        recipient: String,
        subject: String,
        body: String,
        attachment: File
    ) {
        if (!attachment.exists()) {
            println("Attachment file does not exist!")
            return
        }

        val attachmentUri: Uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            attachment
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, attachmentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, "Choose an Email client"))
            hideProgressBar()
        } else {
            println("No email apps available!")
        }
    }

    private fun setupUI() {
        viewBinding.tbSupport.visibility = View.VISIBLE
        viewBinding.tbSupport.title = "Support"

        setSupportActionBar(viewBinding.tbSupport)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewBinding.tbSupport.setNavigationOnClickListener {
            finish()
        }

        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24)
        viewBinding.tbSupport.setNavigationIcon(navigationIcon)

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                viewBinding.tbSupport.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                viewBinding.tbSupport.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                tintNavigationIcon(navigationIcon, R.color.colorWhite)
            }
        }

        viewBinding.ivScreenshot.visibility = View.GONE
        viewBinding.cvScreenshot.visibility = View.GONE

        hideProgressBar()
    }

    private fun getReceiverUserDetails(message: String, developerName: String, developerEmail: String) {
        val appPkg = packageName
        RetrofitClient.apiInterface(mContext).getRolesByAppPkg(appPkg).enqueue(object : Callback<List<Role>> {
            override fun onResponse(call: Call<List<Role>>, response: Response<List<Role>>) {
                if (response.isSuccessful) {
                    val roles = response.body()
                    if (roles != null && roles.isNotEmpty()) {
                        for (role in roles) {
                            if (role.roleType == "ADMIN") {
                                val receiverId = role.userId
                                sendMessage(message, developerName, developerEmail, receiverId)
                                break
                            }
                        }
                    } else {
                        CommonUtils.showToastLong(mContext, "Failed to get receiver details")
                        hideProgressBar()
                    }
                }
            }

            override fun onFailure(call: Call<List<Role>>, t: Throwable) {
                hideProgressBar()
                CommonUtils.apiRequestFailedToast(mContext, t)
            }
        })
    }

    private fun sendMessage(
        message: String,
        developerName: String,
        developerEmail: String,
        receiverId: Int
    ) {
        val chatMessage = ChatMessage(
            chatId = null,
            chatSenderId = SharedPrefsManager.getUserDetails(mContext).userId,
            chatReceiverId = receiverId,
            chatMessage = message,
            chatMedia = "",
            chatTimestamp = null,
            useFlag = true,
        )

        RetrofitClient.apiInterface(mContext).sendMessage(chatMessage).enqueue(object : Callback<ChatMessage> {
            override fun onResponse(call: Call<ChatMessage>, response: Response<ChatMessage>) {
                if (response.isSuccessful) {
                    CommonUtils.showToastLong(mContext, "Message sent successfully")
                    hideProgressBar()
                    viewBinding.tieSupportProblem.text?.clear()
                    sendToChat(receiverId)
                } else {
                    CommonUtils.showToastLong(mContext, "Failed to send message")
                    hideProgressBar()
                }
            }

            override fun onFailure(call: Call<ChatMessage>, t: Throwable) {
                CommonUtils.showToastLong(mContext, "Failed to send message")
                hideProgressBar()
            }
        })
    }

    private fun sendToChat(receiverId: Int) {
        val chatIntent = Intent(mContext, ChatActivity::class.java)
        chatIntent.putExtra("receiver_id", receiverId)
        startActivity(chatIntent)
    }

    private fun hideProgressBar() {
        viewBinding.pbPostAppProgressBar.visibility = View.GONE
        viewBinding.bStartChat.visibility = View.VISIBLE
    }

    private fun showProgressBar() {
        viewBinding.pbPostAppProgressBar.visibility = View.VISIBLE
        viewBinding.bStartChat.visibility = View.GONE
    }

    private fun tintNavigationIcon(icon: Drawable?, colorResId: Int) {
        icon?.let {
            val wrappedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, colorResId))
            viewBinding.tbSupport.navigationIcon = wrappedIcon
        }
    }
}