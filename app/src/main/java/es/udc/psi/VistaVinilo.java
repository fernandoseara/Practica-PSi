package es.udc.psi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import es.udc.psi.databinding.ActivityVistaViniloBinding;


public class VistaVinilo extends AppCompatActivity {

    // Llamo a las cuentas para añadir el vinilo a la lista de vinilos si se desea
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();

    private DatabaseReference usersRef;

    private ActivityVistaViniloBinding binding;

    private final String KEY_ITEM = "contrasena";
    private final String KEY_POS = "sdjnv";

    private String artista = "Artista";
    private String nombre ;
    private String genero;
    private String sello;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vista_vinilo);
        Intent intent = getIntent();

        ArrayList<QueryItem> item_tmp = intent.getParcelableArrayListExtra(KEY_ITEM);

        // Siempre va a ir un objeto
        QueryItem item = item_tmp.get(0);

        this.id = item.getId();
        this.artista = item.getArtista();
        this.nombre = item.getNombre();
        this.genero = item.getGenero();
        this.sello = item.getSello();


        binding.vistaViniloTextoArtista.setText(artista);
        binding.vistaViniloTextoNombre.setText(nombre);
        binding.vistaViniloTextoGenero.setText("Género: " + genero);
        binding.vistaViniloTextoSello.setText("Sello: " + sello);


        // Portada (es un poco más largo)
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference= storageReference.child("portadas/" + item.getId() + ".jpg");
        photoReference.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.vistaViniloPortada.setImageBitmap(bmp);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                binding.vistaViniloPortada.setImageBitmap(null);
            }
        });
    }

    public void anadirLista(View view){

        String UID = currentUser.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        DatabaseReference coleccionRef = usersRef.child(UID).child("collections").child("0");

        long size = 0;
        coleccionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                    // ¿Está esto en la colección?
                    boolean exists = false;
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        if (ds.getValue().toString().equals(id)){
                            exists = true;
                        }
                    }

                    // Sí, se inserta
                    if (exists){
                        long size = dataSnapshot.getChildrenCount();
                        String next_elem = String.valueOf(size);

                        //TODO: Esto cambiaría si añadimos más colecciones
                        coleccionRef.child(next_elem).setValue(id);
                        Toast.makeText(VistaVinilo.this, "Elemento añadido a tu colección correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VistaVinilo.this, "Este elemento ya está en tu colección", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error_db) {
                Toast.makeText(VistaVinilo.this, "Hay un problema con la BD. No puedo añadirlo.", Toast.LENGTH_SHORT).show();
                return;
            }
        });

    }

    public void compartirVinilo(View view){

        String textoACompartir = "Te envío este vinilo desde TocAppDiscos: \n" + artista + " - " + nombre + " (" + sello + ")";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, textoACompartir);
        startActivity(Intent.createChooser(intent, "Compartir usando..."));

    }
}