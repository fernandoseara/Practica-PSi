package es.udc.psi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    /* TODO cambiar a binding */
    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextLastname, editTextDescription;
    Button buttonReg;
    TextView textViewLogin;
    FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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

        editTextEmail = findViewById(R.id.email_reg);
        editTextPassword = findViewById(R.id.password_reg);
        editTextName = findViewById(R.id.user_name);
        editTextLastname = findViewById(R.id.user_lastname);
        editTextDescription = findViewById(R.id.user_description);
        buttonReg = findViewById(R.id.register_but);
        textViewLogin = findViewById(R.id.loginNow);

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
}