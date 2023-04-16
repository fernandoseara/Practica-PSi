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

import es.udc.psi.databinding.ActivityVistaPerfilBinding;

public class VistaPerfil extends AppCompatActivity {

    private ActivityVistaPerfilBinding binding;

    private ViniloAdapter mAdapter;
    private final String KEY_VINILO = "contrasena";
    private final String KEY_POS = "sdjnv";
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_perfil);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vista_perfil);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullname = snapshot.child("name").getValue().toString() + " " +
                            snapshot.child("lastname").getValue().toString();
                    binding.vistaPerfilTextoNombre.setText(fullname);
                    binding.vistaPerfilTextoEmail.setText(snapshot.child("email").getValue()
                            .toString());
                    binding.vistaPerfilTextoDescripcion.setText(snapshot.child("description").getValue()
                            .toString());



                    System.out.println(snapshot.child("collections").getValue());
                    ArrayList<ArrayList<String>> colecciones = (ArrayList<ArrayList<String>>) snapshot.child("collections").getValue();


                    if (colecciones != null) {

                        // Para cada coleccion
                        for (ArrayList<String> coleccion : colecciones) {

                            // TODO: Aún hay que implementar un mecanismo para poder poner más de una colección.

                            ArrayList<Vinilo> initialData = new ArrayList<>();

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

                                initRecycler(initialData);
                            }
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
                    binding.vistaPerfilFotoPerfil.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Inicio recycler con vinilos de prueba
        ArrayList<Vinilo> initialData = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            initialData.add(new Vinilo(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)));
        }
        initRecycler(initialData);


        // Botón de log out
        binding.logoutBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initRecycler(ArrayList<Vinilo> vinilos) {
        mAdapter = new ViniloAdapter(vinilos);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);


        binding.vinilosRv.setLayoutManager(linearLayoutManager);
        binding.vinilosRv.setAdapter(mAdapter);


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
}