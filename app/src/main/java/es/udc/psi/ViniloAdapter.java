package es.udc.psi;

import android.service.autofill.Dataset;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ViniloAdapter extends RecyclerView.Adapter<ViniloAdapter.MyViewHolder>{
    private final ArrayList<Vinilo> mDataset;

    public interface OnItemClickListener {
        public void onClick(View view, int position);
    }

    private static OnItemClickListener clickListener;
    public void setClickListener(OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imagen;
        public TextView nombre;


        public MyViewHolder(View view) {
            super(view);
            imagen = view.findViewById(R.id.vinilo_item_imagen);
            nombre = view.findViewById(R.id.vinilo_item_nombre);
            view.setOnClickListener(this);
        }

        public void bind(Vinilo article) {
            //imagen.setImageBitmap(Noseque);
            nombre.setText(article.getTitulo());
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

    public ViniloAdapter(ArrayList<Vinilo> myDataset) {
        mDataset = myDataset;
    }

    @NonNull
    @Override
    public ViniloAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vinilo_item, parent, false);
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

    public void addItem(Vinilo article) {
        mDataset.add(article);
        notifyItemInserted(mDataset.size()-1);
    }

    public void deleteItem(int position){
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(Vinilo article, int position){
        mDataset.remove(position);
        mDataset.add(position,article);
        notifyItemChanged(position);
    }

    public Vinilo getItem(int position){
        return mDataset.get(position);
    }
}
