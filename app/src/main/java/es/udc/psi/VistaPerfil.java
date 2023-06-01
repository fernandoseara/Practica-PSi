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
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

        if (propio) { // El perfil es el mío

            Log.d("_TAG", "Se está viendo el perfil propio.");
            setContentView(R.layout.activity_vista_perfil_propio);

            binding2 = DataBindingUtil.setContentView(this, R.layout.activity_vista_perfil_propio);

            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {    //  Se está logueado

                        String fullname = snapshot.child("name").getValue(String.class) + " " +
                                snapshot.child("lastname").getValue(String.class);

                        binding2.vistaPerfilTextoNombre.setText(fullname);
                        binding2.vistaPerfilTextoEmail.setText(snapshot.child("email").getValue(String.class));
                        binding2.vistaPerfilTextoDescripcion.setText(snapshot.child("description").getValue(String.class));

                        GenericTypeIndicator<ArrayList<ArrayList<String>>> typeIndicator = new GenericTypeIndicator<ArrayList<ArrayList<String>>>(){};
                        ArrayList<ArrayList<String>> colecciones = snapshot.child("collections").getValue(typeIndicator);

                        System.out.println(colecciones);

                        if (colecciones != null) {

                            binding2.listaDeVinilosTextview.setText(R.string.vistaPerfil_listaVinilos_text);

                            // Para cada coleccion
                            for (ArrayList<String> coleccion : colecciones) {

                                // Para cada vinilo de la coleccion
                                ArrayList<Vinilo> initialData = new ArrayList<>();
                                for(int i = 0; i<coleccion.size(); i++) {

                                    // Pongo su portada en el imageview del recycler
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    StorageReference photoReference= storageReference.child("portadas/" +  coleccion.get(i) + ".jpg");

                                    if(coleccion.get(i) != null) {
                                        int finalI = i;
                                        photoReference.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
                                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            initialData.add(new Vinilo(String.valueOf(coleccion.get(finalI)), bmp));

                                            if (finalI == coleccion.size() - 1) {
                                                initRecycler(initialData, propio);
                                            }

                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // No se saca Toast porque esto se ejecuta cuando se borra un vinilo (está bien)
                                            }
                                        });
                                    }
                                }

                            }
                        }
                    }else{  // No se está logueado -> lanza Login
                        Intent intent_login = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent_login);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(VistaPerfil.this, R.string.falloBD_vistaPerfil_toast, Toast.LENGTH_SHORT).show();
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

            Log.d("_TAG", "Se está viendo un perfil ajeno.");

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
                File localFile = File.createTempFile("profile", ".jpg");
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

            if (coleccion != null) {

                binding.listaDeVinilosTextview.setText(R.string.vistaPerfil_listaVinilos_text);

                // Para cada vinilo de la coleccion
                for (int i = 0; i < coleccion.size(); i++) {

                    // Pongo su portada en el imageview del recycler
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    StorageReference photoReference = storageReference.child("portadas/" + coleccion.get(i) + ".jpg");

                    if (coleccion.get(i) != null) {

                        int finalI = i;
                        photoReference.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            initialData.add(new Vinilo(String.valueOf(coleccion.get(finalI)), bmp));

                            if (finalI == coleccion.size() - 1) {
                                initRecycler(initialData, propio);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(VistaPerfil.this, R.string.falloImagenesColeccionAjena_toast, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
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

                // Mi perfil: ¿Ver vinilo o quitar de colección?
                if(propio){ openOptionMenu(view, pos); }

                // Otro perfil: Que pulsar vinilo me lleve al vinilo
                else{
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
                    return;
                }
            }
        });
    }

    public void openOptionMenu(View v,final int position) {

        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.menu_contextual_rv_perfil, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    // Ver el vinilo abierto
                    case R.id.menuContextualRVPerfil_ver_vinilo:

                        Intent intent = new Intent(getApplicationContext(), VistaVinilo.class);
                        ArrayList<QueryItem> vinilo_item_envio = new ArrayList<>();
                        DocumentReference docRef = db.collection("vinilos").document(mAdapter.getItem(position).getID());

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
                                        intent.putExtra(KEY_POS, position);
                                        startActivity(intent);
                                    }
                                }
                            }
                        });
                        return true;

                    // Quitar el vinilo de la lista
                    case R.id.menuContextualRVPerfil_quitar_lista:

                        // Referencias a BD
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
                        DatabaseReference coleccionRef = mDatabase.child(currentUser.getUid()).child("collections").child("0");

                        // Tomo ID de vinilo pulsado
                        String id_borrar = mAdapter.getItem(position).getID();

                        // Borramos el vinilo concreto (también repetidos)
                        Query removeQuery = coleccionRef;
                        removeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot removeSnapshot : dataSnapshot.getChildren()) {

                                    String valor = removeSnapshot.getValue(String.class);

                                    // Encontrado: Borro
                                    if (Objects.equals(valor, id_borrar)) {
                                        DatabaseReference tmp_ref = removeSnapshot.getRef();
                                        tmp_ref.removeValue();

                                        // Enseño un Snackbar con "deshacer"
                                        Snackbar snackbar = Snackbar.make(v, R.string.viniloEliminado_snackbar, Snackbar.LENGTH_LONG);
                                        snackbar.setAction(R.string.deshacer_eliminarVinilo_snackbar, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                tmp_ref.setValue(id_borrar);
                                            }
                                        });
                                        snackbar.show();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(VistaPerfil.this, R.string.errorBD_borrarVinilo_toast, Toast.LENGTH_SHORT).show();
                            }
                        });

                        return true;
                }
                return true;
            }
        });
        popup.show();

    }

}