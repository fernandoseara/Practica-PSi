package es.udc.psi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

public class MensajeHub extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensaje_hub);

        TabLayout tabLayout = findViewById(R.id.mensaje_hub_tab_layout);
        tabLayout.getTabAt(1).select();

        TabLayout.Tab tab = tabLayout.getTabAt(1);
        if (tab != null) {
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        Intent intent = new Intent(MensajeHub.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

    }
}