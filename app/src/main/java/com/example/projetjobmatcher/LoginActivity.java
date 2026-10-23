package com.example.projetjobmatcher;

// FirebaseAuth permet de connecter l'utilisateur avec email + mot de passe
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText editEmail = findViewById(R.id.editTextEmail);
        EditText editPassword = findViewById(R.id.editTextPassword);
        LinearLayout buttonLogin = findViewById(R.id.buttonLogin);
        TextView textSinscrire = findViewById(R.id.textSinscrire);

        buttonLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            // ── contrôles de saisie ──
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs !", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.setError("Email invalide !");
                editEmail.requestFocus();
                return;
            }

            if (password.length() < 6) {
                editPassword.setError("Minimum 6 caractères !");
                editPassword.requestFocus();
                return;
            }

            // ── connexion Firebase Auth ──
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Email ou mot de passe incorrect !", Toast.LENGTH_LONG).show());
        });

        // bouton s'inscrire séparé du bouton login
        textSinscrire.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}