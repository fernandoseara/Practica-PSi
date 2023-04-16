package es.udc.psi;

import java.util.ArrayList;

public class User {
    public String email;
    public String name;
    public String lastname;
    public String description;
    public ArrayList<ArrayList<String>> collections;


    public User(String email, String name, String lastname, String description) {
        this.email = email;
        this.name = name;
        this.lastname = lastname;
        this.description = description;

        // Añado una colección de prueba
        ArrayList<ArrayList<String>> cols = new ArrayList<>();
        ArrayList<String> col = new ArrayList<>();
        col.add("001");
        cols.add(col);
        this.collections = cols;
    }
}
