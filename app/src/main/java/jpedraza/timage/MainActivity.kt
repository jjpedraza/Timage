package jpedraza.timage
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File


class MainActivity : AppCompatActivity() {
    private val SELECT_FILE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSeleccionar.onClick {
            abrirGaleria()
        }

        btnSubir.onClick {
            val NombreDeMiArchivoEnElServidor = "MiArchivoDesdeAndroid"; // el jpg lo agrega el servidor
            SubirFoto(txtPathReal.text.toString(),NombreDeMiArchivoEnElServidor)
        }
    }



    fun abrirGaleria() { //Seleccionar archivo de foto
        val intent = Intent()
        intent.type = "image/jpeg" //<-- Yo estoy dandole opcion solo para los jpg, puedes cambiar esto, solo tenlo en cuenta en el servidor cuando lo recibas
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Seleccione una imagen"),
            SELECT_FILE
        )
    }


    @SuppressLint("MissingSuperCall") // <---- Recuperar el resultado de la selección de la foto
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                imageView.setImageURI(data?.data)
                val uri: Uri? = data?.data
                val pathReal: String = PathReal(applicationContext, uri)
                val fileName = File(pathReal).name
                txtPathReal.text = pathReal; //<-- Yo la guardo aquí, puedes utilizar otro metodo como las preferencias o alguna variable public
            }
        }
    }

    //Obtener el nombre real del archivo, que ocuparas para enviarlo al archivo
    fun PathReal(context: Context?, contentUri: Uri?): String {
        var cursor =
            contentResolver.query(contentUri!!, null, null, null, null)
        cursor!!.moveToFirst()
        var document_id = cursor.getString(0)
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
        cursor.close()
        cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null
            , MediaStore.Images.Media._ID + " = ? ", arrayOf(document_id), null
        )
        cursor!!.moveToFirst()
        val path =
            cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor.close()
        return path
    }




    //LLAMADOS AL SERVIDOR
    data class Response (
        //Variables del Webservice
        @SerializedName("Exito") var Exito: String,
        @SerializedName("Info") var Info: String,
        @SerializedName("Error") var Error: String

    )


    interface APIService {
        @Multipart
        @POST("recibirFoto.php") //<-- Este archivo es el que recibira el archivo, de allí en adelante programa tu backend
        fun uploadImage(
            @Part("id") id: RequestBody,
            @Part ("file\"; filename=\"myfile.jpg\" ") file: RequestBody
        ): Call<Response>  //<-- clase para manejar las variables que recibes del backend
    }

    private fun SubirFoto(pathReal: String,IdFile: String) {
        doAsync {
            val file = File(pathReal)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val id = RequestBody.create(MediaType.parse("text/plain"), IdFile)
            var api = getRetrofit().create(APIService::class.java).uploadImage(id,requestFile).execute()
            val Respuesta = api.body() as Response
                uiThread {
                    //--- yo manejo tres variables en el backend para informar a las apps lo que sucedio
                    // exito, info y error
                    // lo que suceda de aquí en adelante es tu decicion como manejarlo

                    // ejemplo:
                    // if (Respuesta.Exito = 'TRUE') { ejecuta algo} else {intentalo nuevamente}
                    alert(
                        "Exito = " + Respuesta.Exito + "\n" +
                        "Info = " + Respuesta.Info + "\n" +
                        "Error = " + Respuesta.Error + "\n"
                    ).show()

                }

            }

        }


    //Vinculacion de Retrofit con el servidor, Recuerda poner alguna validación para funcione correctamente
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://plataformaitavu.tamaulipas.gob.mx/test/")
//            .baseUrl("http://172.16.91.131/EnChingaWeb/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}
