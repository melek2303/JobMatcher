package com.example.projetjobmatcher;

                    // ─── importation de firebase  ────
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;


public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // reception des donnees envoyees depuis MainActivity
        String cv    = getIntent().getStringExtra("cv"); // 'cv' contient le texte extrait du PDF par iText7
        String offre = getIntent().getStringExtra("offre");
        TextView textScore = findViewById(R.id.textScore);
        TextView textBadge = findViewById(R.id.textBadge);
        TextView textProfil = findViewById(R.id.textProfil);
        LinearLayout layoutPointsForts = findViewById(R.id.layoutPointsForts);
        LinearLayout layoutAmeliorer  = findViewById(R.id.layoutAmeliorer);
        LinearLayout buttonNouvelle = findViewById(R.id.buttonNouvelle);


                // ── verification textes trop courts ───

        // si le texte extrait du PDF est trop court, l'analyse n'est pas fiable
        if (cv == null || cv.length() < 20 || offre == null || offre.length() < 20) {
            textProfil.setText("Texte trop court pour une analyse fiable !");
            textScore.setText("--");
            return;
        }


        // ── analyse via TextAnalyzer ───
        List<String> keywordsCV    = TextAnalyzer.extractKeywords(cv);  // extractKeywords : supprime stopwords et extrait les mots importants
        List<String> keywordsOffre = TextAnalyzer.extractKeywords(offre);
        int score = TextAnalyzer.calculateScore(keywordsCV, keywordsOffre);  // calculateScore : (mots communs / mots offre) * 100
        textScore.setText(score + "%");


        // ── couleur selon le score ────
        if (score >= 60) {
            textProfil.setText("Bon profil !");
            textBadge.setText("Compatible");
            textBadge.setBackgroundResource(R.drawable.shape_badge_vert);
        }
        else {
            textProfil.setText("Profil insuffisant");
            textBadge.setText("Non compatible");
            textBadge.setBackgroundResource(R.drawable.shape_badge_rouge);
        }


        // ── message si score = 0% ───
        if (score == 0) {
            TextView tvMsg = new TextView(this);
            tvMsg.setText("Aucun mot cle en commun trouve.");
            tvMsg.setTextColor(Color.parseColor("#F44336"));
            tvMsg.setTextSize(15);
            layoutPointsForts.addView(tvMsg);
        }


                    // ── affichage des points forts ──

        // competence validee
        for (String mot : keywordsOffre) {
            if (keywordsCV.contains(mot)) {
                TextView tv = new TextView(this);
                tv.setText("● " + mot + "  ✓");
                tv.setTextColor(Color.parseColor("#4CAF50"));
                tv.setTextSize(15);
                tv.setPadding(0, 8, 0, 8);
                layoutPointsForts.addView(tv);
            }
        }



                // ── affichage des competences manquantes ───

        // a ameliorer
        for (String mot : keywordsOffre) {
            if (!keywordsCV.contains(mot)) {
                TextView tv = new TextView(this);
                tv.setText("● " + mot + "  manque");
                tv.setTextColor(Color.parseColor("#F44336"));
                tv.setTextSize(15);
                tv.setPadding(0, 8, 0, 8);
                layoutAmeliorer.addView(tv);
            }
        }


                        // ── sauvegarde dans Firebase Realtime Database ──

        // chaque analyse est enregistree avec uid + score + date pour l'historique
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("analyses");
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonyme";
        Map<String, Object> analyse = new HashMap<>();
        analyse.put("uid",   uid);
        analyse.put("score", score);
        analyse.put("cv",    cv);
        analyse.put("offre", offre);
        analyse.put("date",  System.currentTimeMillis()); // currentTimeMillis() : horodatage en millisecondes
        db.push().setValue(analyse); // push() : ID unique auto / setValue() : ecrit dans la base



                        // ── btn nouvelle analyse ───
        buttonNouvelle.setOnClickListener(v -> {
            CvManager.getInstance().clearCv(); // reset le CV pour nouvelle analyse
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }
}

