package es.udc.psi;

import java.util.ArrayList;

public class User {
    public String email;
    public String name;
    public String lastname;
    public String description;
    public ArrayList<Integer> collections;


    public User(String email, String name, String lastname, String description) {
        this.email = email;
        this.name = name;
        this.lastname = lastname;
        this.description = description;
        this.collections = new ArrayList<Integer>();
    }
}
