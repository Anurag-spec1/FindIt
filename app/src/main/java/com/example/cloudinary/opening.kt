package com.example.cloudinary

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import carbon.widget.EditText
import carbon.widget.ImageView
import carbon.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


class opening : AppCompatActivity() {

    private lateinit var name: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var searchEditText: EditText
    private lateinit var button: ImageView
    private lateinit var signOutButton: ImageView

    private val imageDataList = mutableListOf<ImageData>()
    private val fullImageList = mutableListOf<ImageData>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        button = findViewById(R.id.addItems)
        name = findViewById(R.id.User_name_home) // ← Make sure this ID matches your layout

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        imageAdapter = ImageAdapter(imageDataList)
        recyclerView.adapter = imageAdapter

        fetchApprovedImages()
        fetchUserName()

        signOutButton = findViewById(R.id.notification_home)

        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // Redirect to login screen
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchApprovedImages() {
        db.collection("images")
            .whereEqualTo("approved", true)
            .addSnapshotListener { snaps, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                fullImageList.clear()
                snaps?.forEach { doc ->
                    val url   = doc.getString("imageUrl")  ?: return@forEach
                    val title = doc.getString("Item_Name") ?: return@forEach
                    val desc  = doc.getString("Item_Description") ?: ""
                    fullImageList.add(ImageData(doc.id, url, title, desc))
                }

                imageDataList.clear()
                imageDataList.addAll(fullImageList)
                imageAdapter.notifyDataSetChanged()
            }
    }

    private fun filter(query: String) {
        val lower = query.lowercase(Locale.getDefault())
        imageDataList.clear()
        if (lower.isEmpty()) {
            imageDataList.addAll(fullImageList)
        } else {
            fullImageList.forEach { item ->
                if (item.title.lowercase(Locale.getDefault()).contains(lower)) {
                    imageDataList.add(item)
                }
            }
        }
        imageAdapter.notifyDataSetChanged()
    }

    private fun fetchUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = FirebaseDatabase.getInstance().getReference("Users").child(uid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").getValue(String::class.java)
                    name.text = "Hi "+userName ?: "Unknown User"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@opening, "Failed to load name", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
