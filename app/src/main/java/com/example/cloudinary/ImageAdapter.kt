package com.example.cloudinary


import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ImageAdapter(
    private var imageDataList: MutableList<ImageData>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private var userAccess: String = ""
    private val dbRef = FirebaseDatabase.getInstance().getReference("Users")
    private val uid   = FirebaseAuth.getInstance().currentUser?.uid
    private val firestore = FirebaseFirestore.getInstance()

    init {
        // fire once to load “access”
        uid?.let {
            dbRef.child(it)
                .child("access")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue(String::class.java)?.let { access ->
                            userAccess = access
                            // now that we have it, refresh list if needed
                            notifyDataSetChanged()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewItem)
        val title: TextView = view.findViewById(R.id.title)
        val check2: ImageView=view.findViewById(R.id.check2)
        val description: TextView=view.findViewById(R.id.description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val data = imageDataList[position]
        // … your existing binding code …

        holder.title.text = data.title
        val prefix = "Description: "
        val ssb = SpannableStringBuilder(prefix + data.description).apply {
            setSpan(
                ForegroundColorSpan(Color.RED),
                0, prefix.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        holder.description.text = ssb

        Glide.with(holder.imageView.context)
            .load(data.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.imageView)

        if (userAccess == "Admin") {
            holder.check2.visibility = View.VISIBLE

            holder.check2.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val toDelete = imageDataList[pos]
                firestore.collection("images")
                    .document(toDelete.docId)
                    .delete()
                    .addOnSuccessListener {
                        imageDataList.removeAt(pos)
                        notifyItemRemoved(pos)
                        notifyItemRangeChanged(pos, imageDataList.size)
                        Toast.makeText(holder.itemView.context, "Image removed", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(holder.itemView.context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }


        } else {

            holder.check2.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = imageDataList.size
}