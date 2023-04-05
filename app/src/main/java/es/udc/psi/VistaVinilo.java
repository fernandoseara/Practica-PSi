package es.udc.psi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import es.udc.psi.databinding.ActivityVistaViniloBinding;


public class VistaVinilo extends AppCompatActivity {

    private ActivityVistaViniloBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vista_vinilo);


    }
}