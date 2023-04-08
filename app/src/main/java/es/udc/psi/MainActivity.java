package es.udc.psi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;

import es.udc.psi.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setup_buscarPerfiles();

        setup_buscarVinilos();

        /*

        TODO: Asi parece que se pueden manejar las imagenes
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("image.png"));
            ImageView imageView = binding.;
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
    }

    public void setup_buscarPerfiles(){

        // Setup de buscar Perfiles
        EditText buscarPerfiles_editText = binding.BuscarPerfilesEditText;
        Button buscarPerfiles_boton = binding.BuscarPerfilesBoton;
        buscarPerfiles_boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String texto = buscarPerfiles_editText.getText().toString();
                // TODO: Hacer la query a la base de datos de Perfiles con ese texto

                Intent intent = new Intent(MainActivity.this, ListaQuery.class);
                // intent.putExtra( DATOS RECIBIDOS DE LA BD )
                startActivity(intent);
            }
        });
    }

    public void setup_buscarVinilos(){

        // Setup de buscar Vinilos
        EditText buscarVinilos_editText = binding.BuscarVinilosEditText;
        Button buscarVinilos_boton = binding.BuscarVinilosBoton;
        buscarVinilos_boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String texto = buscarVinilos_editText.getText().toString();
                // TODO: Hacer la query a la base de datos de Vinilos con ese texto

                Intent intent = new Intent(MainActivity.this, ListaQuery.class);
                // intent.putExtra( DATOS RECIBIDOS DE LA BD )
                startActivity(intent);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_opciones, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.perfil:

                // Clicado item de Perfil -> Lanzar la actividad de ver perfil propio, si no hay
                // sesion iniciada se inicia.

                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, VistaPerfil.class);

                    // Seguramente querramos mandarle las claves para buscar el perfil en la BD aqui
                    //intent.putExtra("ID", "Jose Carlos");

                    startActivity(intent);
                }

                return true;

            case R.id.acerca_de:

                // Clicado item de Acerca De -> Enseñar la info del Acerca De
                new AlertDialog.Builder(this)
                        .setMessage("Autores: \n Fernando Seara Romera\n Sergio Marcos Vázquez\nPedro Pazos Curra\nEduardo Perez Fraguela")
                        .setNegativeButton("Ok", null)
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}