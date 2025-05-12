package br.edu.ifsp.feitopormim.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.edu.ifsp.feitopormim.databinding.ActivityCreatePostBinding
import br.edu.ifsp.feitopormim.util.Base64Converter
import br.edu.ifsp.feitopormim.util.LocalizacaoHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import java.util.UUID

class CreatePostActivity : AppCompatActivity(), LocalizacaoHelper.Callback {
    private lateinit var binding: ActivityCreatePostBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val galeria = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()) {
                uri ->
            if (uri != null) {
                binding.ivPostImage.setImageURI(uri)
            } else {
                Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
            }
        }
        binding.btnChangePhoto.setOnClickListener {
            galeria.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnCreatePost.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()
            if (firebaseAuth.currentUser != null){
                if (binding.cbLocation.isChecked) {
                    solicitarLocalizacao()
                } else {
                    val uuid = UUID.randomUUID().toString()
                    val description = binding.etDescription.text.toString()
                    val imageString = Base64Converter.drawableToString(binding.ivPostImage.
                    drawable)
                    val location = ""
                    val email = firebaseAuth.currentUser!!.email.toString()
                    val dados = hashMapOf(
                        "description" to description,
                        "imageString" to imageString,
                        "useremail" to email,
                        "location" to location
                    )

                    val db = Firebase.firestore
                    db.collection("posts").document(uuid)
                        .set(dados)
                        .addOnSuccessListener {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                }
            }
        }

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun solicitarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val localizacaoHelper = LocalizacaoHelper(applicationContext)
            localizacaoHelper.obterLocalizacaoAtual(this)
        }
    }
    override fun onLocalizacaoRecebida(endereco: Address, latitude: Double,
                                       longitude: Double) {
        runOnUiThread {
            val firebaseAuth = FirebaseAuth.getInstance()
            if (firebaseAuth.currentUser != null) {
                val uuid = UUID.randomUUID().toString()
                val description = binding.etDescription.text.toString()
                val imageString = Base64Converter.drawableToString(
                    binding.ivPostImage.drawable
                )
                val location = endereco.subAdminArea + " " + endereco.countryCode
                val email = firebaseAuth.currentUser!!.email.toString()
                val dados = hashMapOf(
                    "description" to description,
                    "imageString" to imageString,
                    "useremail" to email,
                    "location" to location,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                val db = Firebase.firestore
                db.collection("posts").document(uuid)
                    .set(dados)
                    .addOnSuccessListener {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
            }
        }
    }
    override fun onErro(mensagem: String) {
        System.out.println(mensagem)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions,
            grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            solicitarLocalizacao()
        } else {
            Toast.makeText(this, "Permissão de localização negada",
                Toast.LENGTH_SHORT).show()
        }
    }
}