package es.udc.psi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.udc.psi.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    FirebaseUser currentUser = firebaseAuth.getCurrentUser();

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setup_buscarPerfiles();
        setup_buscarVinilos();

        TabLayout tabLayout = findViewById(R.id.mainActivity_tab_layout);
        tabLayout.getTabAt(0).select();

        // Lógica del tab inferior
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        if (tab != null) {
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 1) {
                        Intent intent = new Intent(MainActivity.this, MensajeHub.class);
                        startActivity(intent);
                    }
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    public void setup_buscarPerfiles(){

        EditText buscarPerfiles_editText = binding.BuscarPerfilesEditText;
        Button buscarPerfiles_boton = binding.BuscarPerfilesBoton;
        buscarPerfiles_boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String texto = buscarPerfiles_editText.getText().toString();
                if(texto.equals("")){
                    Toast.makeText(MainActivity.this, R.string.emptyUser_buscarPerfiles_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Busca email en la base de datos
                final DatabaseReference perfilesRef = FirebaseDatabase.getInstance().getReference("Users");
                Query query = perfilesRef.orderByChild("email").equalTo(texto);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            // Si hay resultados, obtén el primer elemento encontrado
                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();

                            // Saca información del elemento que coincide con la búsqueda
                            String uid = firstChild.getKey();
                            String nombre = firstChild.child("name").getValue(String.class);
                            String apellidos = firstChild.child("lastname").getValue(String.class);
                            String email = firstChild.child("email").getValue(String.class);
                            String descripcion = firstChild.child("description").getValue(String.class);
                            DataSnapshot collectionsSnapshot = firstChild.child("collections");
                            ArrayList<ArrayList<String>> collectionsList = new ArrayList<>();
                            ArrayList<String> coleccion;

                            // Implementación para más colecciones, pero finalmente sólo 1
                            for (DataSnapshot col : collectionsSnapshot.getChildren()) {
                                coleccion = new ArrayList<>();
                                for (DataSnapshot vinilo : col.getChildren()) {
                                    coleccion.add(vinilo.getValue(String.class));
                                }
                                collectionsList.add(coleccion);
                            }

                            // Se crea y lanza el Intent con todos los datos de este perfil
                            Intent intent = new Intent(MainActivity.this, VistaPerfil.class);
                            intent.putExtra("uid", uid);
                            intent.putExtra("email", email);
                            intent.putExtra("nombre", nombre);
                            intent.putExtra("apellidos", apellidos);
                            intent.putExtra("descripcion", descripcion);

                            // Sólo para la primera colección de la lista de colecciones.
                            // Para implementar más colecciones, bastaría con devolver collectionsList.
                            intent.putStringArrayListExtra("colecciones", collectionsList.get(0));

                            startActivity(intent);

                        } else {    // No se encuentra el perfil con ese email
                            Toast.makeText(MainActivity.this, R.string.usuarioNoEncontrado_buscarPerfiles_toast, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { // Fallo de DB
                        Toast.makeText(MainActivity.this, R.string.fallo_bd_buscarPerfiles_toast, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    public void setup_buscarVinilos(){

        EditText buscarVinilos_editText = binding.BuscarVinilosEditText;
        Button buscarVinilos_boton = binding.BuscarVinilosBoton;
        buscarVinilos_boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String input_texto = buscarVinilos_editText.getText().toString();

                // Si editText vacío, avisa.
                if(input_texto.equals("")){
                    Toast.makeText(MainActivity.this, R.string.emptyVinilo_buscarVinilos_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Busca el vinilo en la BD.
                db.collection("vinilos")
                        .whereGreaterThanOrEqualTo("nombre", input_texto)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                // Crea Intent con información del vinilo
                                Intent intent_vinilo = new Intent(MainActivity.this, ListaQuery.class);
                                intent_vinilo.putExtra("busqueda", input_texto);

                                // Envía los elementos del resultado en un Parcelable
                                ArrayList<QueryItem> arrayVinilos = new ArrayList<>();
                                for(QueryDocumentSnapshot doc : task.getResult()){

                                    arrayVinilos.add(new QueryItem(doc.get("ID").toString(),
                                                                    doc.get("nombre").toString(),
                                                                    doc.get("artista").toString(),
                                                                    doc.get("sello").toString(),
                                                                    doc.get("genero").toString()));
                                }
                                intent_vinilo.putParcelableArrayListExtra("resultado", arrayVinilos);
                                startActivity(intent_vinilo);

                            } else { // Error de BD
                                Log.d("_TAG", "", task.getException());
                            }
                        });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_opciones, menu);
        MenuItem perfilMenuItem = menu.findItem(R.id.perfil);

        // Si logueado, se pone el icono de la foto de perfil en el botón del menú
        if(currentUser != null){

            // Buscaremos "[UID].jpg" en la BD para imagen de perfil
            uid = currentUser.getUid();
            StorageReference mStorage = FirebaseStorage.getInstance().getReference();
            StorageReference photoReference = mStorage.child("profilePhotos/"+uid+".jpg");
            try {
                File localFile = File.createTempFile("icono_perfil",".jpg");
                photoReference.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                // Recibida la imagen -> La colocamos
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                                perfilMenuItem.setIcon(drawable);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Fallo de BD -> Ponemos imagen estándar en su lugar
                                perfilMenuItem.setIcon(R.drawable.sin_foto_perfil);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            // Clicado item de Perfil -> Lanzar la actividad de ver perfil propio
            case R.id.perfil:

                // Si logueado -> mi perfil
                FirebaseUser user = auth.getCurrentUser();
                if(user == null) {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);

                }
                // Si no logueado -> login
                else {
                    // Lanzo VistaPerfil con mi propio perfil
                    Intent intent = new Intent(this, VistaPerfil.class);
                    intent.putExtra("email", auth.getCurrentUser().getEmail());
                    startActivity(intent);
                }

                return true;

            // Clicado "Acerca De" -> Enseñar la info del Acerca De
            case R.id.acerca_de:

                new AlertDialog.Builder(this)
                        .setMessage(R.string.autores_acercaDe)
                        .setNegativeButton(R.string.salir_de_acercaDe, null)
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}