package es.udc.psi;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EditPerfil extends AppCompatActivity {

    ImageView editProfilePhoto;
    EditText editTextEmail, editTextName, editTextLastname, editTextDescription;

    Button guardarCambiosButton;
    Button borrarPerfilButton;

    TextView textViewLogin;
    FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfil);

        editProfilePhoto = findViewById(R.id.editProfilePhoto);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextName = findViewById(R.id.editTextName);
        editTextLastname = findViewById(R.id.editTextLastname);
        editTextDescription = findViewById(R.id.editTextDescription);

        guardarCambiosButton = findViewById(R.id.guardarCambiosButton);
        borrarPerfilButton = findViewById(R.id.borrarPerfilButton);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        StorageReference photoReference = mStorage.child("profilePhotos/" +
                currentUser.getUid() + ".jpg");
        try {
            File localFile = File.createTempFile("profile",".jpg");
            photoReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            editProfilePhoto.setImageBitmap(bitmap);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        editProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                my_startActivityForResult.launch(openGalleryIntent);
            }
        });

        guardarCambiosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCambios(v);
            }
        });

        borrarPerfilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarPerfil();
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    editTextEmail.setText(currentUser.getEmail());
                    editTextName.setText(snapshot.child("name").getValue(String.class));
                    editTextLastname.setText(snapshot.child("lastname").getValue(String.class));
                    editTextDescription.setText(snapshot.child("description").getValue(String.class));

                }else{
                    Intent intent_login = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent_login);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditPerfil.this, R.string.fallo_bd_editarPerfil_toast, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void borrarPerfil() {

        // Crear un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.borrarPerfil_title);
        builder.setMessage(R.string.borrarPerfil_mensaje);

        // Botón de cancelar
        builder.setNegativeButton(R.string.borrarPerfil_cancelar, null);

        // Botón de borrar perfil
        builder.setPositiveButton(R.string.borrarPerfil_confirmar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // Borra la foto de perfil almacenada en Storage
                        StorageReference photoReference = mStorage.child("profilePhotos/" +
                                currentUser.getUid() + ".jpg");
                        photoReference.delete();

                        // Borra la información del usuario en Realtime Database
                        usersRef.child(currentUser.getUid()).removeValue();

                        // Borra el usuario de Authentication
                        currentUser.delete();

                        // Listo, vuelve al menú
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void guardarCambios(View v){
        Uri profilePhoto = null;
        if (editProfilePhoto.getTag() != null){
            profilePhoto = Uri.parse(editProfilePhoto.getTag().toString());
        } else {
            String drawableResourceString = "android.resource://es.udc.psi/" + R.drawable.sin_foto_perfil;
            profilePhoto = Uri.parse(drawableResourceString);
        }

        uploadImageToFirebase(profilePhoto, currentUser.getUid());

        String email = editTextEmail.getText().toString();
        String name = editTextName.getText().toString();
        String lastname = editTextLastname.getText().toString();
        String description = editTextDescription.getText().toString();

        // Comprobación de errores
        if(email.isEmpty()){
            editTextEmail.setError(getString(R.string.email_guardarCambios_error));
            editTextEmail.requestFocus();
            return;
        }
        if(name.isEmpty()){
            editTextName.setError(getString(R.string.name_guardarCambios_error));
            editTextName.requestFocus();
            return;
        }

        // Actualizar datos de usuario en Firebase
        usersRef.child(currentUser.getUid()).child("name").setValue(name);
        usersRef.child(currentUser.getUid()).child("lastname").setValue(lastname);
        usersRef.child(currentUser.getUid()).child("description").setValue(description);
        usersRef.child(currentUser.getUid()).child("email").setValue(email);

        // Listo: volvemos al perfil ahora actualizado
        Toast.makeText(this, R.string.editPerfil_done, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, VistaPerfil.class);
        intent.putExtra("email", email);
        startActivity(intent);


    }

    ActivityResultLauncher<Intent> my_startActivityForResult = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), result->{
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        if (selectedImageUri != null) {
                            editProfilePhoto.setImageURI(selectedImageUri);
                            editProfilePhoto.setTag(selectedImageUri.toString());
                        }
                    }
                }
            }
    );

    private void uploadImageToFirebase(Uri imageUri, String uid) {
        String path = "profilePhotos/" + uid + ".jpg";
        StorageReference fileRef = mStorage.child(path);
        fileRef.putFile(imageUri);
    }
}
