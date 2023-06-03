package es.udc.psi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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

        // Recibo la info que me da MainActivity
        Intent intent_recibido = getIntent();

        String texto_busqueda = getString(R.string.titulo_busqueda) + intent_recibido.getStringExtra("busqueda");
        binding.listaQueryTermino.setText(texto_busqueda);

        ArrayList<QueryItem> lista = intent_recibido.getParcelableArrayListExtra("resultado");

        System.out.println(lista);

        // Inicio recycler con items recibidos en llamada.
        ArrayList<QueryItem> initialData;
        if(lista != null)   { initialData = lista; }
        else                { initialData = new ArrayList<>();};

        initRecycler(initialData);
    }

    private void initRecycler(ArrayList<QueryItem> item) {
        mAdapter = new QueryAdapter(item);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        binding.queryRv.setLayoutManager(linearLayoutManager);
        binding.queryRv.setAdapter(mAdapter);
        binding.queryRv.addItemDecoration(new DividerItemDecoration(binding.queryRv.getContext(), DividerItemDecoration.VERTICAL));

        mAdapter.setClickListener((view, pos) -> {

            // Envía el item como un Parcelable extra en un Intent explícito.
            Intent intent_VistaVinilo = new Intent(getApplicationContext(), VistaVinilo.class);
            ArrayList<QueryItem> query_item_envio = new ArrayList<>();
            query_item_envio.add(mAdapter.getItem(pos));
            intent_VistaVinilo.putParcelableArrayListExtra(KEY_ITEM, query_item_envio);

            intent_VistaVinilo.putExtra(KEY_POS, pos);
            startActivity(intent_VistaVinilo);
        });
    }
}