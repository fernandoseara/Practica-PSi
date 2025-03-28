package es.udc.psi;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class QueryItem implements Parcelable {

    // Atributos de Vinilo
    Drawable foto;
    String nombre;
    String artista;
    String sello;
    String genero;
    String id;

    // Forma estándar de definir vinilos
    public QueryItem(String id, String nombre, String artista, String sello, String genero) {
        this.id = id;
        this.nombre = nombre;
        this.artista = artista;
        this.sello = sello;
        this.genero = genero;
    }

    protected QueryItem(Parcel in) {
        nombre = in.readString();
        artista = in.readString();
        sello = in.readString();
        genero = in.readString();
        id = in.readString();
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getSello() {
        return sello;
    }

    public void setSello(String sello) {
        this.sello = sello;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(nombre);
        parcel.writeString(artista);
        parcel.writeString(sello);
        parcel.writeString(genero);
        parcel.writeString(id);
    }
}
