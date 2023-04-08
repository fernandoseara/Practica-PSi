package es.udc.psi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import es.udc.psi.databinding.ActivityVistaPerfilBinding;

public class VistaPerfil extends AppCompatActivity {

    private ActivityVistaPerfilBinding binding;

    private ViniloAdapter mAdapter;
    private final String KEY_VINILO = "contrasena";
    private final String KEY_POS = "sdjnv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_perfil);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vista_perfil);


        // Inicio recycler con 10 vinilos de prueba
        ArrayList<Vinilo> initialData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            initialData.add(new Vinilo("Vinilo de Prueba (sin foto)", "Prueba"));
        }
        initRecycler(initialData);
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
                startActivityForResult(intent,1);
            }
        });

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
}