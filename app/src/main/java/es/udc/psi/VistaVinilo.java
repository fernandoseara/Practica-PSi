package es.udc.psi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import es.udc.psi.databinding.ActivityVistaViniloBinding;


public class VistaVinilo extends AppCompatActivity {

    private ActivityVistaViniloBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vista_vinilo);


    }

    public void compartirVinilo(View view){

        // TODO: Llamada a la BD aquí
        String artista = "Nombre Artista";
        String vinilo = "Nombre Vinilo";

        String textoACompartir = artista + " - " + vinilo;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, textoACompartir);
        startActivity(Intent.createChooser(intent, "Compartir usando"));

    }
}