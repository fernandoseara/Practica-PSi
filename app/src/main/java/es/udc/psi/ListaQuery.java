package es.udc.psi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import es.udc.psi.databinding.ActivityListaQueryBinding;
import es.udc.psi.databinding.ActivityVistaPerfilBinding;

public class ListaQuery extends AppCompatActivity {

    private ActivityListaQueryBinding binding;
    private QueryAdapter mAdapter;
    private final String KEY_ITEM = "contrasena";
    private final String KEY_POS = "sdjnv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_query);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lista_query);

        // Inicio recycler con 10 items de prueba
        ArrayList<QueryItem> initialData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            initialData.add(new QueryItem("Vinilo de prueba (sin foto) " + i));
        }
        initRecycler(initialData);
    }

    private void initRecycler(ArrayList<QueryItem> item) {
        mAdapter = new QueryAdapter(item);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        binding.queryRv.setLayoutManager(linearLayoutManager);
        binding.queryRv.setAdapter(mAdapter);

        mAdapter.setClickListener(new QueryAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int pos) {
                Log.d("_TAG", " Item " + pos );

                // TODO: Si perfil, lanza VistaPerfil.class. Si vinilo, lanza VistaVinilo.class
                Intent intent = new Intent(getApplicationContext(), VistaVinilo.class);

                intent.putExtra(KEY_ITEM, mAdapter.getItem(pos));
                intent.putExtra(KEY_POS, pos);
                startActivity(intent);
            }
        });
    }
}