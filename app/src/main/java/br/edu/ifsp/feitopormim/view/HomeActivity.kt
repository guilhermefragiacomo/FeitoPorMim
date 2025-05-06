package br.edu.ifsp.feitopormim.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.feitopormim.databinding.ActivityHomeBinding
import br.edu.ifsp.feitopormim.model.Post
import br.edu.ifsp.feitopormim.util.Base64Converter
import br.edu.ifsp.feitopormim.view.adapter.PostAdapter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val email = firebaseAuth.currentUser!!.email.toString()
        db.collection("users").document(email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val document = task.result
                    val imageString = document.data!!["user_image"].toString()
                    val bitmap = Base64Converter.stringToBitmap(imageString)
                    binding.ivProfileLogo.setImageBitmap(bitmap)
                    binding.etUserName.text = document.data!!["username"].toString()
                }
            }

        binding.ivProfileLogo.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        fun loadFeed() {
            val db = Firebase.firestore
            db.collection("posts").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val document = task.result
                        val posts = ArrayList<Post>()
                        for (document in document.documents) {
                            val imageString = document.data!!["imageString"].toString()
                            val bitmap = Base64Converter.stringToBitmap(imageString)
                            val description = document.data!!["description"].toString()
                            val email = document.data!!["useremail"].toString()
                            val location = document.data!!["location"].toString()
                            Log.e("Sistema", email)
                            db.collection("users").document(email).get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful){
                                        val userDocument = task.result
                                        val userImageString = userDocument.data!!["user_image"].toString()
                                        val userBitmap = Base64Converter.stringToBitmap(userImageString)
                                        val username = userDocument.data!!["username"].toString()
                                        Log.e("Sistema", username)

                                        posts.add(Post(description, bitmap, userBitmap, username, location))

                                        val adapter = PostAdapter(posts.toTypedArray())
                                        binding.postsReciclerView.layoutManager = LinearLayoutManager(this)
                                        binding.postsReciclerView.adapter = adapter
                                    }
                                }
                        }
                    }
                }
        }

        binding.btnPosts.setOnClickListener {
            loadFeed()
        }

        binding.btnCreatePost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
            finish()
        }

        loadFeed()
    }
}