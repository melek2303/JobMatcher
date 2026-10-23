package com.example.projetjobmatcher;

                    // ─── importation de firebase  ──
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;



public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // cnx des variables aux elements de l'ecran XML
        EditText editNom = findViewById(R.id.editTextNom);
        EditText editEmail = findViewById(R.id.editTextEmail);
        EditText editPassword = findViewById(R.id.editTextPassword);
        CheckBox checkBox = findViewById(R.id.checkBoxConditions);
        LinearLayout buttonRegister = findViewById(R.id.buttonRegister);
        TextView textLogin = findViewById(R.id.textLogin);

        //clic sue le btn d'inscri
        buttonRegister.setOnClickListener(v -> {
            String nom = editNom.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            // controle de saisie
            if (nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,"Veuillez remplir tous les champs !",Toast.LENGTH_SHORT).show();
                return;
            }
            if (nom.length() < 3) {
                editNom.setError("Nom trop court !"); editNom.requestFocus(); return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.setError("Email invalide !"); editEmail.requestFocus(); return;
            }
            if (password.length() < 6) {
                editPassword.setError("Minimum 6 caracteres !"); editPassword.requestFocus(); return;
            }
            if (!checkBox.isChecked()) {   Toast.makeText(this,"Veuillez accepter les conditions !",Toast.LENGTH_SHORT).show();
                return;
            }


                            // creation du compte Firebase Auth

            FirebaseAuth auth = FirebaseAuth.getInstance(); // getInstance() : recupere l'instance unique (Singleton) de Firebase Auth
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(result -> {
                        String uid = result.getUser().getUid();

                        // Sauvegarde du nom dans Realtime Database
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users"); // getReference("users") : noeud 'users' dans la base
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("nom", nom);
                        userData.put("email", email);
                        db.child(uid).setValue(userData).addOnSuccessListener(unused -> { // child(uid).setValue() : cree le noeud users/uid avec les donnees
                                    Toast.makeText(this,"Compte cree avec succes !",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,"Erreur : "+e.getMessage(),Toast.LENGTH_LONG).show());

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,"Erreur : "+e.getMessage(),Toast.LENGTH_LONG).show());
        });
        textLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}