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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private final String KEY_VINILO = "contrasena";
    private final String KEY_POS = "sdjnv";
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    private boolean propio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        // ¿Es el mío este perfil?
        if (currentUser!= null) {
            boolean propio = Objects.equals(currentUser.getEmail(), email);
        }

        if (propio) { // El perfil es el mío

            setContentView(R.layout.activity_vista_perfil_propio);

            binding2 = DataBindingUtil.setContentView(this, R.layout.activity_vista_perfil_propio);

            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser()
                    .getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String fullname = snapshot.child("name").getValue().toString() + " " +
                                snapshot.child("lastname").getValue().toString();
                        binding2.vistaPerfilTextoNombre.setText(fullname);
                        binding2.vistaPerfilTextoEmail.setText(snapshot.child("email").getValue()
                                .toString());
                        binding2.vistaPerfilTextoDescripcion.setText(snapshot.child("description").getValue()
                                .toString());

                        ArrayList<ArrayList<String>> colecciones = (ArrayList<ArrayList<String>>) snapshot.child("collections").getValue();

                        if (colecciones != null) {

                            // Para cada coleccion
                            for (ArrayList<String> coleccion : colecciones) {

                                // TODO: Aún hay que implementar un mecanismo para poder poner más de una colección.

                                ArrayList<Vinilo> initialData = new ArrayList<>();

                                initialData.add(new Vinilo(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)));

                                // Para cada vinilo de la coleccion
                                for(int i = 0; i<coleccion.size(); i++) {

                                    // Pongo su portada en el imageview del recycler
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    StorageReference photoReference= storageReference.child("portadas/" +  coleccion.get(i) + ".jpg");

                                    try{
                                        File portadaFile = File.createTempFile("portada" + i, ".jpg");
                                        photoReference.getFile(portadaFile)
                                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                        Bitmap bmp = BitmapFactory.decodeFile(portadaFile.getAbsolutePath());
                                                        initialData.add(new Vinilo(bmp));
                                                    }
                                                });
                                    }catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }


                                }
                                initRecycler(initialData, propio);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(VistaPerfil.this, "La base de datos está fallando. ¿Tienes conexión?", Toast.LENGTH_SHORT).show();
                }
            });
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
        }
        else { // El perfil no es el mío

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

            ArrayList<Vinilo> initialData = new ArrayList<>();

            initialData.add(new Vinilo(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)));

            // Para cada vinilo de la coleccion
            for(int i = 0; i < coleccion.size(); i++) {

                // Pongo su portada en el imageview del recycler
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference photoReference= storageReference.child("portadas/" +  coleccion.get(i) + ".jpg");

                try{
                    File portadaFile = File.createTempFile("portada" + i, ".jpg");
                    photoReference.getFile(portadaFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Bitmap bmp = BitmapFactory.decodeFile(portadaFile.getAbsolutePath());
                                    initialData.add(new Vinilo(bmp));
                                }
                            });
                }catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            initRecycler(initialData, propio);
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

    /*
        mAdapter.setClickListener(new ViniloAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int pos) {
                Log.d("_TAG", " Item " + pos );

                Intent intent = new Intent(getApplicationContext(), VistaVinilo.class);
                intent.putExtra(KEY_VINILO, mAdapter.getItem(pos));
                intent.putExtra(KEY_POS, pos);
                startActivity(intent);
            }
        });

    }

     */
    }
}