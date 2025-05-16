package com.example.cloudinary


//MvS8dpXqR62gyMvuJowom9Z40z4
//231725269526121

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
//import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import carbon.widget.Button
import carbon.widget.EditText
import carbon.widget.ImageView
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import okhttp3.Credentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var buttonBack: ImageView
    private lateinit var imageView: ImageView
    private lateinit var text1: EditText
    private lateinit var text2: EditText

    private val REQUEST_IMAGE = 1001
    private val PERMISSION_REQUEST = 2001
    private var cameraUri: Uri? = null
    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            showImagePickerChooser()
        } else {
            showToast("Camera & storage permissions are required")
            // Show explanation if needed
            permissions.entries.forEach { (permission, isGranted) ->
                if (!isGranted && shouldShowRequestPermissionRationale(permission)) {
                    showToast("$permission is required to select or take photos")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Cloudinary
        initCloudinary()

        imageView = findViewById(R.id.imageView)
        button = findViewById(R.id.Upload)
        text1 = findViewById(R.id.itemName)
        text2 = findViewById(R.id.itemDescription)
        buttonBack = findViewById(R.id.backBtn)

        buttonBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        imageView.setOnClickListener { checkPermissionsAndShowChooser() }

        button.setOnClickListener {
            val name = text1.text.toString().trim()
            val desc = text2.text.toString().trim()
            if (name.isNotEmpty() && desc.isNotEmpty()) {
                imageUri?.let { uri -> uploadImageToCloudinary(uri, name, desc) }
                    ?: showToast("Please select or capture an image first!")
            } else {
                showToast("All fields are required!")
            }
        }
    }

    private fun initCloudinary() {
        try {
            val config = HashMap<String, String>().apply {
                put("cloud_name", "dukncxhvq")
                put("api_key", "231725269526121")
                put("api_secret", "MvS8dpXqR62gyMvuJowom9Z40z4")
            }
            MediaManager.init(this, config)
        } catch (e: Exception) {
            Log.e("CloudinaryInit", "Error initializing Cloudinary", e)
            showToast("Error initializing image upload service")
        }
    }

    private fun checkPermissionsAndShowChooser() {
        val requiredPermissions = mutableListOf<String>().apply {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            showImagePickerChooser()
        } else {
            Log.d("Permissions", "Requesting: ${missingPermissions.joinToString()}")
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun showImagePickerChooser() {
        try {
            // Prepare camera output file
            val photoFile = File.createTempFile(
                "IMG_${System.currentTimeMillis()}",
                ".jpg",
                externalCacheDir ?: cacheDir
            ).apply {
                createNewFile()
            }

            cameraUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )

            // Gallery intent
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }

            // Camera intent
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            // Create chooser
            val chooserIntent = Intent.createChooser(galleryIntent, "Select Image Source")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
            startActivityForResult(chooserIntent, REQUEST_IMAGE)

        } catch (e: Exception) {
            Log.e("ImageChooser", "Error creating file or intent", e)
            showToast("Error: ${e.localizedMessage ?: "Failed to open image picker"}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                imageUri = when {
                    data?.data != null -> data.data // From gallery
                    cameraUri != null -> cameraUri // From camera
                    else -> null
                }

                imageUri?.let {
                    Log.d("ImageURI", "Selected image URI: $it")
                    imageView.setImageURI(it)
                } ?: showToast("Failed to get image")

            } catch (e: Exception) {
                Log.e("ImageLoad", "Error loading image", e)
                showToast("Error loading image")
            }
        }
    }

    private fun uploadImageToCloudinary(uri: Uri, name: String, description: String) {
        showToast("Uploading image...")

        try {
            // Create upload request
            val uploadRequest = MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("Upload", "Upload started")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100 / totalBytes).toInt()
                        Log.d("Upload", "Progress: $progress%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                        Log.d("Upload", "Upload successful")
                        val imageUrl = resultData["secure_url"]?.toString() ?: resultData["url"]?.toString()
                        if (imageUrl != null) {
                            saveImageToFirestore(imageUrl, name, description)
                        } else {
                            showToast("Upload failed: No URL returned")
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("Upload", "Error: ${error.description}")
                        showToast("Upload failed: ${error.description}")
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.d("Upload", "Rescheduling upload")
                    }
                })
                .option("upload_preset", "ml_default") // Add your upload preset here


            uploadRequest.dispatch()
        } catch (e: Exception) {
            Log.e("Upload", "Error starting upload", e)
            showToast("Error starting upload: ${e.localizedMessage}")
        }
    }

    private fun saveImageToFirestore(imageUrl: String, itemName: String, desc: String) {
        val imageData = hashMapOf(
            "imageUrl" to imageUrl,
            "Item_Name" to itemName,
            "Item_Description" to desc,
            "approved" to false,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("images")
            .add(imageData)
            .addOnSuccessListener {
                showToast("Upload successful!")
                startActivity(Intent(this, opening::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving document", e)
                showToast("Failed to save: ${e.localizedMessage}")
            }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}