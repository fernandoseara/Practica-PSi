package es.udc.psi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

        // TODO: Recoger la foto y ponerla
        //binding.vistaViniloPortada.setImageDrawable( noseque );
    }

    public void compartirVinilo(View view){

        // TODO: Llamada a la BD aquí

        String textoACompartir = artista + " - " + nombre + " (" + sello + ")";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, textoACompartir);
        startActivity(Intent.createChooser(intent, "Compartir usando"));

    }
}