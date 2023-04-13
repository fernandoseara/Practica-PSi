package es.udc.psi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import es.udc.psi.databinding.ActivityVistaViniloBinding;


public class VistaVinilo extends AppCompatActivity {

    private ActivityVistaViniloBinding binding;

    private final String KEY_ITEM = "contrasena";
    private final String KEY_POS = "sdjnv";

    private String artista = "Artista";
    private String nombre ;
    private String genero;
    private String sello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vista_vinilo);
        Intent intent = getIntent();

        ArrayList<QueryItem> item_tmp = intent.getParcelableArrayListExtra(KEY_ITEM);
        QueryItem item = item_tmp.get(0);
        this.artista = item.getArtista();
        this.nombre = item.getNombre();
        this.genero = item.getGenero();
        this.sello = item.getSello();


        binding.vistaViniloTextoArtista.setText(artista);
        binding.vistaViniloTextoNombre.setText(nombre);
        binding.vistaViniloTextoGenero.setText("Género: " + genero);
        binding.vistaViniloTextoSello.setText("Sello: " + sello);


        // Portada (es un poco más largo)
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference= storageReference.child("portadas/" + item.getId() + ".jpg");
        photoReference.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.vistaViniloPortada.setImageBitmap(bmp);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                binding.vistaViniloPortada.setImageBitmap(null);
            }
        });

    }

    public void compartirVinilo(View view){

        String textoACompartir = artista + " - " + nombre + " (" + sello + ")";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, textoACompartir);
        startActivity(Intent.createChooser(intent, "Compartir usando..."));

    }
}