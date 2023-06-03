package es.udc.psi;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Vinilo implements Parcelable {

    Bitmap portada;

    String ID;
    String titulo;
    String description ;

    // Método estándar para definir el item de vinilo en las colecciones del perfil
    public Vinilo(String ID, Bitmap portada){
        this.ID = ID;
        this.portada = portada;
    }

    protected Vinilo(Parcel in) {
        titulo = in.readString();
        ID = in.readString();
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

    public Bitmap getPortada() {
        return portada;
    }

    public String getID() {
        return ID;
    }

    public void setPortada(Bitmap portada) { this.portada = portada; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(titulo);
        parcel.writeString(ID);
        parcel.writeString(description);
    }
}
