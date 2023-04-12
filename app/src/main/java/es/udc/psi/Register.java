package es.udc.psi;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    /* TODO cambiar a binding */
    ImageView imageViewPhoto;
    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextLastname, editTextDescription;
    Button buttonReg;
    TextView textViewLogin;
    FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            /* TODO cambiar a la activity que queramos */
            Intent intent = new Intent(getApplicationContext(), VistaPerfil.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        imageViewPhoto = findViewById(R.id.profile_photo_reg);
        editTextEmail = findViewById(R.id.email_reg);
        editTextPassword = findViewById(R.id.password_reg);
        editTextName = findViewById(R.id.user_name);
        editTextLastname = findViewById(R.id.user_lastname);
        editTextDescription = findViewById(R.id.user_description);
        buttonReg = findViewById(R.id.register_but);
        textViewLogin = findViewById(R.id.loginNow);

        imageViewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                my_startActivityForResult.launch(openGalleryIntent);
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, name, lastname, description;

                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                name = String.valueOf(editTextName.getText());
                lastname = String.valueOf(editTextLastname.getText());
                description = String.valueOf(editTextDescription.getText());


                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                        TextUtils.isEmpty(name) || TextUtils.isEmpty(lastname) ||
                        TextUtils.isEmpty(description)) {
                    Toast.makeText(Register.this, "Enter all fields",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        // Log.d(TAG, "createUserWithEmail:success");
                                        // FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(Register.this, "Authentication successful.",
                                                Toast.LENGTH_SHORT).show();

                                        String uid = mAuth.getCurrentUser().getUid();

                                        Uri profilePhoto = Uri.parse(imageViewPhoto.getTag().toString());
                                        uploadImageToFirebase(profilePhoto, uid);

                                        createUserData(uid, email, name, lastname, description);

                                        Intent intent = new Intent(getApplicationContext(), VistaPerfil.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Register.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    private void createUserData(String id, String email, String name, String lastname,
                                String description){
        User user = new User(email,name,lastname,description);

        mDatabase.child("Users").child(id).setValue(user);
    }

    ActivityResultLauncher<Intent> my_startActivityForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        if (selectedImageUri != null) {
                            imageViewPhoto.setImageURI(selectedImageUri);
                            imageViewPhoto.setTag(selectedImageUri.toString());
                        }
                    }
                }
            }
    );

    private void uploadImageToFirebase(Uri imageUri, String uid) {
        /* TODO establecer path foto */
        String path = "profilePhotos/" + uid + ".jpg";
        StorageReference fileRef = mStorage.child(path);
        fileRef.putFile(imageUri);

        /*

        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        })

         */
    }
}