package es.udc.psi;

import android.media.Image;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Vinilo implements Parcelable {

    Image portada;
    String titulo;
    String description ;

    public Vinilo(String titulo, Image portada, String description) {
        this.titulo = titulo;
        this.portada = portada;
        this.description = description;
    }

    protected Vinilo(Parcel in) {
        titulo = in.readString();
        //portada = in.read();      // TODO: Habria que ver como se haria esto
        description = in.readString();
    }

    public static final Creator<Vinilo> CREATOR = new Creator<Vinilo>() {
        @Override
        public Vinilo createFromParcel(Parcel in) {
            return new Vinilo(in);
        }

        @Override
        public Vinilo[] newArray(int size) {
            return new Vinilo[size];
        }
    };

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Image getPortada() {
        return portada;
    }

    public void setPortada(Image portada) {
        this.portada = portada;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(titulo);
        // parcel. TODO: meter imagen de alguna forma
        parcel.writeString(description);
    }
}
