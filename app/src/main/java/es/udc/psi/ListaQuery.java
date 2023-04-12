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
    private String modo = "Vinilo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_query);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lista_query);

        // Recibo la info que me da MainActivity
        Intent intent = getIntent();
        this.modo = intent.getStringExtra("modo");

        String texto_busqueda = "Resultados de la búsqueda:  " + intent.getStringExtra("busqueda");
        binding.listaQueryTermino.setText(texto_busqueda);

        ArrayList<QueryItem> lista = intent.getParcelableArrayListExtra("resultado");

        System.out.println(lista);

        // Inicio recycler con items recibidos en llamada.
        ArrayList<QueryItem> initialData;
        if(lista != null)   { initialData = lista; }
        else                { initialData = new ArrayList<>();};

        for (int i = 0; i < 10; i++) {
            initialData.add(new QueryItem( modo + " de prueba (sin foto) " + i));
        }
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
            Log.d("_TAG", " Item " + pos );

            Intent intent = new Intent(getApplicationContext(), VistaPerfil.class);

            if (Objects.equals(modo, "Vinilo")) {
                intent = new Intent(getApplicationContext(), VistaVinilo.class);
            }

            // Esto parece muy raro pero es la forma más cómoda de mandar el item
            ArrayList<QueryItem> query_item_envio = new ArrayList<>();
            query_item_envio.add(mAdapter.getItem(pos));
            intent.putParcelableArrayListExtra(KEY_ITEM, query_item_envio);


            intent.putExtra(KEY_POS, pos);
            startActivity(intent);
        });
    }
}