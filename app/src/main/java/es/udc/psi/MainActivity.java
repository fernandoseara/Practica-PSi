package es.udc.psi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

                // Clicado item de Perfil -> Lanzar la actividad de ver perfil propio.

                return true;

            case R.id.acerca_de:

                // Clicado item de Acerca De -> Enseñar la info del Acerca De

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}