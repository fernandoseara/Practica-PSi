package es.udc.psi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

public class EditPerfil extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextName, editTextLastname, editTextDescription;

    Button guardarCambiosButton;
    Button borrarPerfilButton;

    TextView textViewLogin;
    FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfil);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextName = findViewById(R.id.editTextName);
        editTextLastname = findViewById(R.id.editTextLastname);
        editTextDescription = findViewById(R.id.editTextDescription);

        guardarCambiosButton = findViewById(R.id.guardarCambiosButton);
        borrarPerfilButton = findViewById(R.id.borrarPerfilButton);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        guardarCambiosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCambios();
            }
        });

        borrarPerfilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarPerfil();
            }
        });

        editTextEmail.setText(currentUser.getEmail());
        editTextName.setText(currentUser.getDisplayName());
        editTextLastname.setText(currentUser.getDisplayName());
        editTextDescription.setText(currentUser.getDisplayName());
    }

    private void borrarPerfil() {
        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                usersRef.child(currentUser.getUid()).removeValue();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void guardarCambios(){
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String name = editTextName.getText().toString();
        String lastname = editTextLastname.getText().toString();
        String description = editTextDescription.getText().toString();

        if(email.isEmpty()){
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if(name.isEmpty()){
            editTextName.setError("Name is required");
            editTextName.requestFocus();
            return;
        }

        if(lastname.isEmpty()){
            editTextLastname.setError("Lastname is required");
            editTextLastname.requestFocus();
            return;
        }

        if(description.isEmpty()){
            editTextDescription.setError("Description is required");
            editTextDescription.requestFocus();
            return;
        }

        //Actualizar datos de usuario en Firebase
        usersRef.child("name").setValue(name);
        usersRef.child("lastname").setValue(lastname);
        usersRef.child("description").setValue(description);
        usersRef.child("email").setValue(email);
        usersRef.child("password").setValue(password);

        Toast.makeText(this, "User updated", Toast.LENGTH_LONG).show();
    }
}
