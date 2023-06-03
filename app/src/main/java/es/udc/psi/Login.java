package es.udc.psi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
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

public class Login extends AppCompatActivity {

    TextInputEditText email_EditText, password_EditText;
    Button login_Button;
    TextView register_TextView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void onStart() {
        super.onStart();

        // Si ya se está logueado, no pintamos nada aquí -> vemos nuestro perfil
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent_VistaPerfil = new Intent(getApplicationContext(), VistaPerfil.class);
            intent_VistaPerfil.putExtra("email", currentUser.getEmail());
            startActivity(intent_VistaPerfil);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Elementos de la vista
        email_EditText = findViewById(R.id.email_log);
        password_EditText = findViewById(R.id.password_log);
        login_Button = findViewById(R.id.login_but);
        register_TextView = findViewById(R.id.registerNow);

        // "Register" es pulsable -> Lanza la actividad Register
        register_TextView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        login_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Se toma el input
                String email_input, password_input;
                email_input = String.valueOf(email_EditText.getText());
                password_input = String.valueOf(password_EditText.getText());

                // Ambos campos son obligatorios
                if (TextUtils.isEmpty(email_input) || TextUtils.isEmpty(password_input)) {
                    Toast.makeText(Login.this, R.string.login_empty_emailOrPass_toast,
                            Toast.LENGTH_SHORT).show();
                } else {

                    // Se pide a la BD el Login
                    mAuth.signInWithEmailAndPassword(email_input, password_input)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(Login.this, R.string.login_success_toast,
                                                Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {

                                        Toast.makeText(Login.this, R.string.login_fail_toast,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent_MainActivity = new Intent(this, MainActivity.class);
            intent_MainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent_MainActivity);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}