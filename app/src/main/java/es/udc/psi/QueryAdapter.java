package es.udc.psi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class QueryAdapter extends RecyclerView.Adapter<QueryAdapter.MyViewHolder>{

    private final ArrayList<QueryItem> mDataset;
    public interface OnItemClickListener {
        void onClick(View view, int position);
    }
    private static OnItemClickListener clickListener;
    public void setClickListener(OnItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView imagen_item;
        public TextView texto_item;
        public TextView artista_item;

        public MyViewHolder(View view) {
            super(view);

            // Inicializo elementos de la vista de cada QueryItem
            imagen_item = view.findViewById(R.id.query_item_imagen);
            texto_item = view.findViewById(R.id.query_item_nombre);
            artista_item = view.findViewById(R.id.query_item_artista);
            view.setOnClickListener(this);
        }

        public void bind(QueryItem article) {

            // Se coloca la información en la vista
            try {

                // Referencias e instancias a BD
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference portadaRef = storage.getReference().child("portadas/" + article.getId() + ".jpg");
                Log.d("_TAG", article.getId() + ".jpg");
                portadaRef.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imagen_item.setImageBitmap(bmp);
                }).addOnFailureListener(exception -> imagen_item.setImageBitmap(null));

            }catch (Exception e){   // Si BD falla, imagen estándar
                imagen_item.setImageResource(R.drawable.sin_portada);
            }
            texto_item.setText(article.getNombre());
            artista_item.setText(article.getArtista());
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

    public QueryAdapter(ArrayList<QueryItem> myDataset) {
        mDataset = myDataset;
    }

    @NonNull
    @Override
    public QueryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.query_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(mDataset.get(position));
    }

    @Override
    public int getItemCount(){
        return mDataset.size();
    }

    public void addItem(QueryItem article) {
        mDataset.add(article);
        notifyItemInserted(mDataset.size()-1);
    }

    public void deleteItem(int position){
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(QueryItem article, int position){
        mDataset.remove(position);
        mDataset.add(position,article);
        notifyItemChanged(position);
    }

    public QueryItem getItem(int position){
        return mDataset.get(position);
    }
}
