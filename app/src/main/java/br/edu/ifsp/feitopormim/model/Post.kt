package br.edu.ifsp.feitopormim.model

import android.graphics.Bitmap

class Post (private val descricao: String, private val foto: Bitmap, private val userImage: Bitmap, private val userName: String, private val location: String){
    public fun getDescricao() : String{
        return descricao
    }
    public fun getFoto() : Bitmap {
        return foto
    }
    public fun getUserImage() : Bitmap {
        return userImage
    }
    public fun getUserName() : String {
        return userName
    }
    public fun getLocation() : String {
        return location
    }
}