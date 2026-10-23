package com.example.projetjobmatcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CV = 301;

    private EditText editNomProfil;
    private TextView textEmailProfil;
    private TextView textNomCV;
    private ProgressBar progressBarCV;

    private FirebaseUser user;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ── Bind views ──
        editNomProfil    = findViewById(R.id.editNomProfil);
        textEmailProfil  = findViewById(R.id.textEmailProfil);
        textNomCV        = findViewById(R.id.textNomCV);
        progressBarCV    = findViewById(R.id.progressBarCV);
        LinearLayout btnSauvegarder  = findViewById(R.id.buttonSauvegarder);
        LinearLayout btnChangerMdp   = findViewById(R.id.buttonChangerMdp);
        LinearLayout btnImporterCV   = findViewById(R.id.buttonImporterCV);
        LinearLayout btnRetour       = findViewById(R.id.buttonRetourProfil);
        LinearLayout btnSupprimer    = findViewById(R.id.buttonSupprimer);

        // ── Firebase ──
        user    = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(user.getUid());

        // ── Charger email ──
        textEmailProfil.setText(user.getEmail());

        // ── Charger nom depuis Firebase ──
        userRef.child("nom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String nom = snapshot.getValue(String.class);
                if (nom != null) editNomProfil.setText(nom);
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        // ── Afficher CV importé ──
        if (CvManager.getInstance().hasCv()) {
            textNomCV.setText("CV importé ✓");
        }

        // ── Sauvegarder le nom ──
        btnSauvegarder.setOnClickListener(v -> {
            String nouveauNom = editNomProfil.getText().toString().trim();
            if (nouveauNom.isEmpty()) {
                editNomProfil.setError("Nom vide !");
                return;
            }
            userRef.child("nom").setValue(nouveauNom)
                    .addOnSuccessListener(u ->
                            Toast.makeText(this, "Nom mis à jour ✓", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // ── Changer mot de passe ──
        btnChangerMdp.setOnClickListener(v -> {
            String email = user.getEmail();
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(u ->
                            Toast.makeText(this,
                                    "Email de réinitialisation envoyé à " + email,
                                    Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erreur : " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });

        // ── Importer CV ──
        btnImporterCV.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(
                    Intent.createChooser(intent, "Sélectionner votre CV (PDF)"),
                    REQUEST_CV
            );
        });

        // ── Retour ──
        btnRetour.setOnClickListener(v -> finish());

        // ── Supprimer le compte ──
        btnSupprimer.setOnClickListener(v -> supprimerCompte());
    }

    // ── Import CV ────────────────────────────────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CV && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String mimeType = getContentResolver().getType(uri);
            if ("application/pdf".equals(mimeType)) {
                CvManager.getInstance().setCvUri(uri);
                textNomCV.setText("CV importé ✓");
                Toast.makeText(this, "CV importé avec succès ✓", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Veuillez sélectionner un fichier PDF",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ── Supprimer compte ─────────────────────────────────────────────────────
    private void supprimerCompte() {
        // Demander confirmation d'abord
        new android.app.AlertDialog.Builder(this)
                .setTitle("Supprimer le compte")
                .setMessage("Êtes-vous sûr ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> demanderMotDePasse())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void demanderMotDePasse() {
        // Demander le mot de passe pour ré-authentifier
        android.widget.EditText editMdp = new android.widget.EditText(this);
        editMdp.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        editMdp.setHint("Votre mot de passe");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Confirmer votre identité")
                .setMessage("Entrez votre mot de passe pour continuer :")
                .setView(editMdp)
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    String mdp = editMdp.getText().toString().trim();
                    if (mdp.isEmpty()) {
                        Toast.makeText(this, "Mot de passe vide !", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    reAuthentifierEtSupprimer(mdp);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void reAuthentifierEtSupprimer(String mdp) {
        // Ré-authentification avec email + mot de passe
        com.google.firebase.auth.AuthCredential credential =
                com.google.firebase.auth.EmailAuthProvider
                        .getCredential(user.getEmail(), mdp);

        user.reauthenticate(credential)
                .addOnSuccessListener(u -> {
                    // ✅ Ré-auth réussie → supprimer les données puis le compte
                    userRef.removeValue();
                    CvManager.getInstance().clearCv();

                    user.delete().addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Compte supprimé ✓", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, SplashActivity.class);
                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );
                        startActivity(intent);
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, "Erreur : " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
                })
                .addOnFailureListener(e ->
                        // ❌ Mauvais mot de passe
                        Toast.makeText(this, "Mot de passe incorrect !",
                                Toast.LENGTH_LONG).show()
                );
    }
}