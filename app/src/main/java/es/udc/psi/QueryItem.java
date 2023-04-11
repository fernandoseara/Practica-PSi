package es.udc.psi;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class QueryItem implements Parcelable {

    Drawable foto;
    String texto;

    String artista;


    public QueryItem(String texto, Drawable foto) {
        this.texto = texto;
        this.foto = foto;

    }

    public QueryItem(String texto) {
        this.texto = texto;
        this.foto = null;
    }

    public QueryItem(String texto, String artista) {
        this.texto = texto;
        this.artista = artista;

    }

    public QueryItem(String texto, String artista, Drawable foto) {
        this.texto = texto;
        this.artista = artista;
        this.foto = foto;

    }

    protected QueryItem(Parcel in) {
        texto = in.readString();
        artista = in.readString();
        //portada = in.read();      // TODO: Habria que ver como se haria esto

    }

    public static final Creator<QueryItem> CREATOR = new Creator<QueryItem>() {
        @Override
        public QueryItem createFromParcel(Parcel in) {
            return new QueryItem(in);
        }

        @Override
        public QueryItem[] newArray(int size) {
            return new QueryItem[size];
        }
    };

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getArtista() {
        return this.artista;
    }

    public void setArtista(String texto) {
        this.artista = texto;
    }

    public Drawable getFoto() {
        return foto;
    }

    public void setFoto(Drawable foto) {
        this.foto = foto;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(texto);
        parcel.writeString(artista);
        // parcel. TODO: meter imagen de alguna forma

    }
}
