package br.edu.ifsp.feitopormim.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.feitopormim.R
import br.edu.ifsp.feitopormim.model.Post

class PostAdapter(private val posts: Array<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val imgPost : ImageView = view.findViewById(R.id.ivPostImage)
        val txtDescricao : TextView = view.findViewById(R.id.tvDescription)
        val userImage : ImageView = view.findViewById(R.id.ivProfileLogo)
        val userName : TextView = view.findViewById(R.id.tvUserName)
        val location : TextView = view.findViewById(R.id.tvLocation)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return posts.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtDescricao.text = posts[position].getDescricao()
        holder.imgPost.setImageBitmap(posts[position].getFoto())
        holder.userImage.setImageBitmap(posts[position].getUserImage())
        holder.userName.text = posts[position].getUserName()
        holder.location.text = posts[position].getLocation()
    }
}