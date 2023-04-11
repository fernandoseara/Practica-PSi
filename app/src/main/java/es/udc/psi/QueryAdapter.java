package es.udc.psi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class QueryAdapter extends RecyclerView.Adapter<QueryAdapter.MyViewHolder>{
    private final ArrayList<QueryItem> mDataset;

    public interface OnItemClickListener {
        public void onClick(View view, int position);
    }

    private static OnItemClickListener clickListener;
    public void setClickListener(OnItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // TODO: Aqui poner la info que creamos conveniente sobre cada búsqueda
        public ImageView imagen;
        public TextView texto;

        public TextView artista;


        public MyViewHolder(View view) {
            super(view);
            imagen = view.findViewById(R.id.query_item_imagen);
            texto = view.findViewById(R.id.query_item_nombre);
            artista = view.findViewById(R.id.query_item_artista);
            view.setOnClickListener(this);
        }

        public void bind(QueryItem article) {
            imagen.setImageDrawable(article.getFoto());
            texto.setText(article.getTexto());
            artista.setText(article.getArtista());
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
