package es.udc.psi;

import android.content.Intent;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Register extends AppCompatActivity {
    ImageView foto_ImageView;
    TextInputEditText email_EditText, password_EditText, name_EditText, lastname_EditText, description_EditText;
    Button register_Button;
    TextView login_TextView;
    FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    public void onStart() {
        super.onStart();

        // Si ya se está logueado, no pintamos nada aquí -> vemos nuestro perfil
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), VistaPerfil.class);
            intent.putExtra("email", currentUser.getEmail());
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Referencias e instancias a BD
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        // Elementos de la vista
        foto_ImageView = findViewById(R.id.profile_photo_reg);
        email_EditText = findViewById(R.id.email_reg);
        password_EditText = findViewById(R.id.password_reg);
        name_EditText = findViewById(R.id.user_name);
        lastname_EditText = findViewById(R.id.user_lastname);
        description_EditText = findViewById(R.id.user_description);
        register_Button = findViewById(R.id.register_but);
        login_TextView = findViewById(R.id.loginNow);

        // Click en la foto deja escoger foto con el método estándar del OS
        foto_ImageView.setOnClickListener(view -> {
            Intent openGalleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            my_startActivityForResult.launch(openGalleryIntent);
        });

        // Click en "Login" lanza actividad Login
        login_TextView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        register_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email_input, password_input, name_input, lastname_input, description_input;

                email_input = String.valueOf(email_EditText.getText());
                password_input = String.valueOf(password_EditText.getText());
                name_input = String.valueOf(name_EditText.getText());
                lastname_input = String.valueOf(lastname_EditText.getText());
                description_input = String.valueOf(description_EditText.getText());

                // Email y Password son obligatorios
                if (TextUtils.isEmpty(email_input) || TextUtils.isEmpty(password_input) ||
                        TextUtils.isEmpty(name_input)) {
                    Toast.makeText(Register.this, R.string.emptyFields_register_toast,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Si está en orden, se crea el usuario
                    mAuth.createUserWithEmailAndPassword(email_input, password_input)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(Register.this, R.string.registroSuccess_toast,
                                                Toast.LENGTH_SHORT).show();

                                        String uid = mAuth.getCurrentUser().getUid();

                                        // Manejo de la foto de perfil
                                        Uri profilePhoto;
                                        if(foto_ImageView.getTag() != null){
                                            profilePhoto = Uri.parse(foto_ImageView.getTag().toString());
                                        }
                                        else{
                                            String drawableResourceString = "android.resource://es.udc.psi/" + R.drawable.sin_foto_perfil;
                                            profilePhoto = Uri.parse(drawableResourceString);
                                        }
                                        uploadImageToFirebase(profilePhoto, uid);

                                        // Listo: Creo el objeto User con estos datos y lanzo este perfil.
                                        createUserData(uid, email_input, name_input, lastname_input, description_input);
                                        Intent intent = new Intent(getApplicationContext(), VistaPerfil.class);
                                        intent.putExtra("email", email_input);
                                        startActivity(intent);
                                        finish();

                                    } else { // Falla la creación
                                        Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Register.this, R.string.registerFailed_toast,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    private void createUserData(String id, String email, String name, String lastname, String description){
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
                            foto_ImageView.setImageURI(selectedImageUri);
                            foto_ImageView.setTag(selectedImageUri.toString());
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