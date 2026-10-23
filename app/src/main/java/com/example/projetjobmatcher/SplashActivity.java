package com.example.projetjobmatcher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Si déjà connecté → aller directement à MainActivity
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return; // ne pas afficher le splash
        }

        setContentView(R.layout.activity_splash);

        LinearLayout buttonSinscrire   = findViewById(R.id.buttonSinscrire);
        LinearLayout buttonSeConnecter = findViewById(R.id.buttonSeConnecter);

        buttonSinscrire.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        buttonSeConnecter.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}