package es.udc.psi;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class EditPerfil extends AppCompatActivity {

    EditText editTextEmail, editTextName, editTextLastname, editTextDescription;

    Button guardarCambiosButton;
    Button borrarPerfilButton;

    TextView textViewLogin;
    FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfil);

        editTextEmail = findViewById(R.id.editTextEmail);
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
                Toast.makeText(EditPerfil.this, "La base de datos está fallando. ¿Tienes conexión?", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void borrarPerfil() {

        // Crear un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Borrar perfil?");
        builder.setMessage("¿Seguro que quieres borrar este perfil? Esta acción no puede deshacerse (en serio).");

        // Botón de cancelar
        builder.setNegativeButton("¡No! Cancelar", null);

        // Botón de borrar perfil
        builder.setPositiveButton("Sí, hazlo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        usersRef.child(currentUser.getUid()).removeValue();
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
        String email = editTextEmail.getText().toString();
        String name = editTextName.getText().toString();
        String lastname = editTextLastname.getText().toString();
        String description = editTextDescription.getText().toString();

        // Comprobación de errores
        if(email.isEmpty()){
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
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

        // Listo: volvemos al perfil ahora actualizado
        Toast.makeText(this, R.string.editPerfil_done, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, VistaPerfil.class);
        intent.putExtra("email", email);
        startActivity(intent);


    }
}
