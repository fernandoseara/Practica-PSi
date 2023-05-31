package es.udc.psi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import es.udc.psi.databinding.ActivityVistaPerfilBinding;
import es.udc.psi.databinding.ActivityVistaPerfilPropioBinding;

public class VistaPerfil extends AppCompatActivity {

    private ActivityVistaPerfilBinding binding;
    private ActivityVistaPerfilPropioBinding binding2;

    private ViniloAdapter mAdapter;
    private final String KEY_ITEM = "contrasena";
    private final String KEY_POS = "sdjnv";
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean propio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        // ¿Es el mío este perfil?
        if (currentUser != null) {
            propio = Objects.equals(currentUser.getEmail(), email);
        }else{
            Intent intent_login = new Intent(getApplicationContext(), Login.class);
            startActivity(intent_login);
        }

        Toast.makeText(this, email, Toast.LENGTH_SHORT).show();
        if (propio) { // El perfil es el mío

            Log.d("_TAG", "Se está viendo el prefil propio.");
            setContentView(R.layout.activity_vista_perfil_propio);

            binding2 = DataBindingUtil.setContentView(this, R.layout.activity_vista_perfil_propio);

            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String fullname = snapshot.child("name").getValue(String.class) + " " +
                                snapshot.child("lastname").getValue(String.class);

                        binding2.vistaPerfilTextoNombre.setText(fullname);
                        binding2.vistaPerfilTextoEmail.setText(snapshot.child("email").getValue(String.class));
                        binding2.vistaPerfilTextoDescripcion.setText(snapshot.child("description").getValue(String.class));

                        GenericTypeIndicator<ArrayList<ArrayList<String>>> typeIndicator = new GenericTypeIndicator<ArrayList<ArrayList<String>>>() {};
                        ArrayList<ArrayList<String>> colecciones = snapshot.child("collections").getValue(typeIndicator);

                        if (colecciones != null) {

                            binding2.listaDeVinilosTextview.setText(R.string.vistaPerfil_listaVinilos_text);

                            // Para cada coleccion
                            for (ArrayList<String> coleccion : colecciones) {

                                // TODO: Aún hay que implementar un mecanismo para poder poner más de una colección.

                                ArrayList<Vinilo> initialData = new ArrayList<>();

                                //initialData.add("000", new Vinilo(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)));

                                // Para cada vinilo de la coleccion
                                for(int i = 0; i<coleccion.size(); i++) {

                                    // Pongo su portada en el imageview del recycler
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    StorageReference photoReference= storageReference.child("portadas/" +  coleccion.get(i) + ".jpg");

                                    int finalI = i;
                                    photoReference.getBytes(1024*1024).addOnSuccessListener(bytes -> {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        initialData.add(new Vinilo(String.valueOf(coleccion.get(finalI)), bmp));
                                    });

                                    /*try{
                                        File portadaFile = File.createTempFile("portada" + i, ".jpg");
                                        String finalI = String.valueOf(i);
                                        photoReference.getFile(portadaFile)
                                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                        Bitmap bmp = BitmapFactory.decodeFile(portadaFile.getAbsolutePath());
                                                        initialData.add(new Vinilo(finalI, bmp));
                                                    }
                                                });
                                    }catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }*/

                                }
                                initialData.add(new Vinilo("-1",Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)));
                                initRecycler(initialData, propio);
                            }
                        }
                    }else{
                        Intent intent_login = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent_login);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(VistaPerfil.this, "La base de datos está fallando. ¿Tienes conexión?", Toast.LENGTH_SHORT).show();
                }
            });

            // Foto de perfil
            mStorage = FirebaseStorage.getInstance().getReference();
            StorageReference photoReference = mStorage.child("profilePhotos/" +
                    FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");

            try {
                File localFile = File.createTempFile("profile",".jpg");
                photoReference.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                binding2.vistaPerfilFotoPerfil.setImageBitmap(bitmap);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Botón de log out
            binding2.logoutBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            binding2.editBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(VistaPerfil.this, EditPerfil.class);
                    startActivity(intent);
                }
            });
        }
        else { // El perfil no es el mío

            Log.d("_TAG", "Se está viendo un prefil ajeno.");

            setContentView(R.layout.activity_vista_perfil);
            binding = DataBindingUtil.setContentView(this, R.layout.activity_vista_perfil);

            String nombre = intent.getStringExtra("nombre");
            String apellidos = intent.getStringExtra("apellidos");
            String uid = intent.getStringExtra("uid");
            String descripcion = intent.getStringExtra("descripcion");
            ArrayList<String> coleccion = intent.getStringArrayListExtra("colecciones");

            binding.vistaPerfilTextoNombre.setText(nombre + " " + apellidos);
            binding.vistaPerfilTextoEmail.setText(email);
            binding.vistaPerfilTextoDescripcion.setText(descripcion);


            // Foto de perfil
            mStorage = FirebaseStorage.getInstance().getReference();
            StorageReference profilePhotoReference = mStorage.child("profilePhotos/" + uid + ".jpg");
            try {
                File localFile = File.createTempFile("profile",".jpg");
                profilePhotoReference.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                binding.vistaPerfilFotoPerfil.setImageBitmap(bitmap);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Recyclerview de colección
            ArrayList<Vinilo> initialData = new ArrayList<>();
            System.out.println("BBBBBBBBBBBBBB    " + coleccion);
            if(coleccion != null) {

                binding.listaDeVinilosTextview.setText(R.string.vistaPerfil_listaVinilos_text);

                // Para cada vinilo de la coleccion
                for (int i = 0; i < coleccion.size(); i++) {

                    // Pongo su portada en el imageview del recycler
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    StorageReference photoReference = storageReference.child("portadas/" + coleccion.get(i) + ".jpg");

                    int finalI = i;
                    photoReference.getBytes(1024*1024).addOnSuccessListener(bytes -> {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        initialData.add(new Vinilo(String.valueOf(finalI), bmp));
                    });

                    /*try {
                        File portadaFile = File.createTempFile("portada" + i, ".jpg");
                        String finalI = String.valueOf(i);
                        photoReference.getFile(portadaFile)
                                .addOnSuccessListener(taskSnapshot -> {
                                    Bitmap bmp = BitmapFactory.decodeFile(portadaFile.getAbsolutePath());
                                    initialData.add(new Vinilo(finalI, bmp));
                                })
                                .addOnFailureListener(exception -> {
                                    Toast.makeText(this, "No soy capaz de cargar esta imagen", Toast.LENGTH_SHORT).show();
                                });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/
                }
                initialData.add(new Vinilo("-1",Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)));
                initRecycler(initialData, propio);
            }
        }
    }

    private void initRecycler(ArrayList<Vinilo> vinilos, boolean propio) {
        mAdapter = new ViniloAdapter(vinilos);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        if (propio){
            binding2.vinilosRv.setLayoutManager(linearLayoutManager);
            binding2.vinilosRv.setAdapter(mAdapter);
        } else {
            binding.vinilosRv.setLayoutManager(linearLayoutManager);
            binding.vinilosRv.setAdapter(mAdapter);
        }

        mAdapter.setClickListener(new ViniloAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int pos) {
                Log.d("_TAG", " Item " + pos );

                Intent intent = new Intent(getApplicationContext(), VistaVinilo.class);
                ArrayList<QueryItem> vinilo_item_envio = new ArrayList<>();
                DocumentReference docRef = db.collection("vinilos").document(mAdapter.getItem(pos).getID());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                vinilo_item_envio.add(new QueryItem(doc.get("ID").toString(),
                                        doc.get("nombre").toString(),
                                        doc.get("artista").toString(),
                                        doc.get("sello").toString(),
                                        doc.get("genero").toString()));

                                intent.putParcelableArrayListExtra(KEY_ITEM, vinilo_item_envio);
                                intent.putExtra(KEY_POS, pos);
                                startActivity(intent);
                            }
                        }
                    }
                });
            }
        });

    }
}