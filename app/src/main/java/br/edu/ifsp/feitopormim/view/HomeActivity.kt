package br.edu.ifsp.feitopormim.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.feitopormim.databinding.ActivityHomeBinding
import br.edu.ifsp.feitopormim.model.Post
import br.edu.ifsp.feitopormim.util.Base64Converter
import br.edu.ifsp.feitopormim.view.adapter.PostAdapter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    private val db = Firebase.firestore
    private val pageSize = 5

    private val pageCursors = mutableListOf<Pair<DocumentSnapshot?, DocumentSnapshot?>>()
    private var currentPageIndex = 0

    private var searchField = "location"
    private var searchFilter = ""

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



        binding.btnNextPage.setOnClickListener {
            loadFeed(act = 1, searchField, searchFilter)
        }

        binding.btnBackPage.setOnClickListener {
            loadFeed(act = -1, searchField, searchFilter)
        }

        binding.btnPosts.setOnClickListener {
            loadFeed(act = 3, searchField, searchFilter)
        }

        binding.btnSearchCity.setOnClickListener {
            searchFilter = binding.etSearchCity.text.toString()

            loadFeed(act = 0, searchField, searchFilter)
        }

        binding.btnCreatePost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
            finish()
        }

        loadFeed(act = 0, searchField, searchFilter)
    }

    private fun loadFeed(act: Int, field: String = "", filter: String = "") {
        var query = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(pageSize.toLong())
        if (filter.isNotEmpty() && field.isNotEmpty()) {
            query = db.collection("posts")
                .whereEqualTo(field, filter)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
        }


        if (act == 1) {
            if (currentPageIndex < pageCursors.size) {
                val lastDoc = pageCursors[currentPageIndex].second
                lastDoc?.let {
                    query = query.startAfter(it)
                }
            }
        } else if (act == -1 && currentPageIndex > 0) {
            val prevDoc = pageCursors[currentPageIndex - 1].first
            prevDoc?.let {
                query = query.startAt(it)
            }
        }

        query.get().addOnSuccessListener { result ->
            if (result.isEmpty) return@addOnSuccessListener

            val documents = result.documents
            val posts = ArrayList<Post>()

            var loadedCount = 0

            for (doc in documents) {
                val imageString = doc["imageString"].toString()
                val bitmap = Base64Converter.stringToBitmap(imageString)
                val description = doc["description"].toString()
                val email = doc["useremail"].toString()
                val location = doc["location"].toString()

                db.collection("users").document(email).get().addOnSuccessListener { userDoc ->
                    val userImageString = userDoc["user_image"].toString()
                    val userBitmap = Base64Converter.stringToBitmap(userImageString)
                    val username = userDoc["username"].toString()

                    posts.add(Post(description, bitmap, userBitmap, username, location))

                    loadedCount++
                    if (loadedCount == documents.size) {
                        val adapter = PostAdapter(posts.toTypedArray())
                        binding.postsReciclerView.layoutManager = LinearLayoutManager(this)
                        binding.postsReciclerView.adapter = adapter

                        if (act == 1) {
                            if (currentPageIndex == pageCursors.size) {
                                val firstDoc = documents.firstOrNull()
                                val lastDoc = documents.lastOrNull()
                                pageCursors.add(Pair(firstDoc, lastDoc))
                            }
                            currentPageIndex++
                        } else if (act == -1) {
                            if (currentPageIndex > 0) {
                                currentPageIndex--
                            }
                        } else if (act == 0){
                            val firstDoc = documents.firstOrNull()
                            val lastDoc = documents.lastOrNull()
                            pageCursors.add(Pair(firstDoc, lastDoc))
                        }

                        binding.btnNextPage.visibility = if (documents.size < 5) View.GONE else View.VISIBLE
                        binding.btnBackPage.visibility = if (currentPageIndex > 0) View.VISIBLE else View.GONE

                        Log.e("System-feitoPorMim", currentPageIndex.toString() + " " + pageCursors.size.toString())
                    }
                }
            }
        }
    }
}