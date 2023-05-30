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
    private FirebaseAuth auth;
    private FirebaseUser user;
    private StorageReference mStorage;

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

        // Setup de buscar Perfiles
        EditText buscarPerfiles_editText = binding.BuscarPerfilesEditText;
        Button buscarPerfiles_boton = binding.BuscarPerfilesBoton;
        buscarPerfiles_boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String texto = buscarPerfiles_editText.getText().toString();
                if(texto.equals("")){
                    Toast.makeText(MainActivity.this, "Por favor, pon algo en el texto editable", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Busco texto en base de datos
                final DatabaseReference perfilesRef = FirebaseDatabase.getInstance().getReference("Users");
                Query query = perfilesRef.orderByChild("email").equalTo(texto);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            // Si hay resultados, obtén el primer elemento encontrado
                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();

                            // Aquí puedes obtener el elemento que coincide con la búsqueda
                            String uid = firstChild.getKey();
                            String nombre = firstChild.child("name").getValue(String.class);
                            String apellidos = firstChild.child("lastname").getValue(String.class);
                            String email = firstChild.child("email").getValue(String.class);
                            String descripcion = firstChild.child("description").getValue(String.class);

                            DataSnapshot collectionsSnapshot = firstChild.child("collections");
                            ArrayList<ArrayList<String>> collectionsList = new ArrayList<>();
                            ArrayList<String> coleccion = new ArrayList<>();

                            for (DataSnapshot col : collectionsSnapshot.getChildren()) {

                                coleccion = new ArrayList<>();
                                for (DataSnapshot vinilo : col.getChildren()) {
                                    coleccion.add(vinilo.getValue(String.class));
                                }
                                collectionsList.add(coleccion);
                            }

                            Intent intent = new Intent(MainActivity.this, VistaPerfil.class);
                            intent.putExtra("uid", uid);
                            intent.putExtra("email", email);
                            intent.putExtra("nombre", nombre);
                            intent.putExtra("apellidos", apellidos);
                            intent.putExtra("descripcion", descripcion);

                            // TODO: Por ahora sólo una colección
                            intent.putStringArrayListExtra("colecciones", collectionsList.get(0));

                            startActivity(intent);

                        } else {

                            Toast.makeText(MainActivity.this, "Este perfil no existe, ¿estás escribiéndolo bien?", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Manejar cualquier error de consulta aquí
                    }
                });

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

                if(texto.equals("")){
                    Toast.makeText(MainActivity.this, "Por favor, pon algo en el texto editable", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("vinilos")
                        .whereGreaterThanOrEqualTo("nombre", texto)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(MainActivity.this, ListaQuery.class);
                                intent.putExtra("busqueda", texto);
                                intent.putExtra("modo", "Vinilo");

                                ArrayList<QueryItem> arrayVinilos = new ArrayList<>();

                                for(QueryDocumentSnapshot doc : task.getResult()){

                                    arrayVinilos.add(new QueryItem(doc.get("ID").toString(),
                                                                    doc.get("nombre").toString(),
                                                                    doc.get("artista").toString(),
                                                                    doc.get("sello").toString(),
                                                                    doc.get("genero").toString()));

                                    Log.d("_TAG", "Paso a la lista " + doc.get("nombre").toString() + " (" + doc.get("ID").toString() + ")");
                                }
                                intent.putParcelableArrayListExtra("resultado", arrayVinilos);
                                startActivity(intent);

                            } else {
                                Log.d("_TAG", "Error getting documents: ", task.getException());
                            }
                        });

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_opciones, menu);

        MenuItem perfilMenuItem = menu.findItem(R.id.perfil);

        // Si está registrado, se pone el icono de la foto de perfil en el botón del menú
        if(currentUser != null){
            uid = currentUser.getUid();

            mStorage = FirebaseStorage.getInstance().getReference();
            StorageReference photoReference = mStorage.child("profilePhotos/" +
                    uid + ".jpg");

            try {
                File localFile = File.createTempFile("icono_perfil",".jpg");
                photoReference.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                                perfilMenuItem.setIcon(drawable);
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
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.perfil:

                // Clicado item de Perfil -> Lanzar la actividad de ver perfil propio, si no hay
                // sesion iniciada se inicia.

                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();

                if(user == null) {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);

                } else {
                    // Lanzo VistaPerfil con mi propio perfil
                    Intent intent = new Intent(this, VistaPerfil.class);
                    intent.putExtra("email", auth.getCurrentUser().getEmail());
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