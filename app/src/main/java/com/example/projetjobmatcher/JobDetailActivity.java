package com.example.projetjobmatcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.InputStream;

public class JobDetailActivity extends AppCompatActivity {

    public static final String EXTRA_JOB_ID = "job_id";
    private JobOffer job;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        int jobId = getIntent().getIntExtra(EXTRA_JOB_ID, -1);
        job = findJobById(jobId);
        if (job == null) { finish(); return; }

        bindData();
        setupButtons();
    }

    private void bindData() {
        ((TextView) findViewById(R.id.tvDetailTitle)).setText(job.getTitle());
        ((TextView) findViewById(R.id.tvDetailCompany)).setText(job.getCompany());
        ((TextView) findViewById(R.id.tvDetailLocation)).setText(job.getLocation());
        ((TextView) findViewById(R.id.tvDetailContract)).setText(job.getContractType());
        ((TextView) findViewById(R.id.tvDetailDate)).setText(job.getPostedDate());
        ((TextView) findViewById(R.id.tvDetailDescription)).setText(job.getDescription());

        LinearLayout rowSalary = findViewById(R.id.rowDetailSalary);
        if (job.hasSalary()) {
            rowSalary.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvDetailSalary)).setText(job.getSalary());
        } else {
            rowSalary.setVisibility(View.GONE);
        }

        // Afficher le score si CV déjà importé
        if (CvManager.getInstance().hasCv()) {
            LinearLayout rowCompat = findViewById(R.id.rowCompatibility);
            rowCompat.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvScore)).setText("CV prêt ✓");
        }

        updateBookmarkIcon();
    }

    private void setupButtons() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ImageButton btnBookmark = findViewById(R.id.btnBookmarkDetail);
        btnBookmark.setOnClickListener(v -> {
            job.setSaved(!job.isSaved());
            updateBookmarkIcon();
            Toast.makeText(this,
                    job.isSaved() ? "Offre sauvegardée" : "Offre retirée",
                    Toast.LENGTH_SHORT).show();
        });

        Button btnApply = findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> {
            boolean hasCv = CvManager.getInstance().hasCv();

            if (!hasCv) {
                Intent intent = new Intent(this, ApplyActivity.class);
                // ✅ Utilise la constante directement pour éviter toute erreur de clé
                intent.putExtra("apply_job_id", job.getId());
                startActivityForResult(intent, 200);
            } else {
                lancerAnalyse();
            }
        });
    }

    // ── Retour depuis ApplyActivity après import CV ───────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // ApplyActivity a importé un CV → lancer l'analyse automatiquement
        if (requestCode == 200 && CvManager.getInstance().hasCv()) {
            lancerAnalyse();
        }
    }

    // ── Lancer ResultActivity avec cv + offre ─────────────────────────────────
    private void lancerAnalyse() {
        Uri cvUri = CvManager.getInstance().getCvUri();
        if (cvUri == null) {
            Toast.makeText(this, "Erreur : CV introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        String cvTexte = lirePdfAvecItext(cvUri);

        if (cvTexte.isEmpty()) {
            Toast.makeText(this, "Impossible de lire le PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        String offreTexte = job.getTitle() + " " + job.getDescription();

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("cv", cvTexte);
        intent.putExtra("offre", offreTexte);
        startActivity(intent);
    }

    // ── Lecture PDF avec iText7 ───────────────────────────────────────────────
    private String lirePdfAvecItext(Uri uri) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            PdfReader reader = new PdfReader(is);
            PdfDocument pdfDoc = new PdfDocument(reader);

            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                sb.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i)));
                sb.append(" ");
            }
            pdfDoc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }

    private void updateBookmarkIcon() {
        ImageButton btn = findViewById(R.id.btnBookmarkDetail);
        int color = job.isSaved()
                ? ContextCompat.getColor(this, R.color.accent_blue)
                : ContextCompat.getColor(this, R.color.text_muted);
        btn.setColorFilter(color);
    }

    private JobOffer findJobById(int id) {
        for (JobOffer j : SampleData.getSampleJobs()) {
            if (j.getId() == id) return j;
        }
        return null;
    }
}