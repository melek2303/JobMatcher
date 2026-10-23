package com.example.projetjobmatcher;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ApplyActivity extends AppCompatActivity {

    public static final String EXTRA_JOB_ID = "apply_job_id"; // ✅ même valeur
    private static final int REQUEST_CV = 201;

    private LinearLayout layoutUploadZone;
    private LinearLayout layoutFileSelected;
    private TextView tvFileName;
    private TextView tvFileSize;
    private Button btnSubmit;
    private android.widget.ImageButton btnRemoveFile;

    private Uri selectedCvUri;
    private JobOffer job;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        int jobId = getIntent().getIntExtra(EXTRA_JOB_ID, -1);
        Toast.makeText(this, "Job ID reçu : " + jobId, Toast.LENGTH_SHORT).show(); // log temporaire
        job = findJobById(jobId);
        if (job == null) {
            Toast.makeText(this, "Job introuvable !", Toast.LENGTH_SHORT).show(); // log temporaire
            finish();
            return;
        }
        bindViews();
        setupSubtitle();
        setupButtons();

        if (CvManager.getInstance().hasCv()) {
            selectedCvUri = CvManager.getInstance().getCvUri();
            showFileSelected(selectedCvUri);
        }
    }

    private void bindViews() {
        layoutUploadZone   = findViewById(R.id.layoutUploadZone);
        layoutFileSelected = findViewById(R.id.layoutFileSelected);
        tvFileName         = findViewById(R.id.tvFileName);
        tvFileSize         = findViewById(R.id.tvFileSize);
        btnSubmit          = findViewById(R.id.btnSubmit);
        btnRemoveFile = (android.widget.ImageButton) findViewById(R.id.btnRemoveFile);
    }

    private void setupSubtitle() {
        TextView tvSubtitle = findViewById(R.id.tvApplySubtitle);
        tvSubtitle.setText(job.getTitle() + " – " + job.getCompany());
    }

    private void setupButtons() {
        findViewById(R.id.btnBackApply).setOnClickListener(v -> finish());
        layoutUploadZone.setOnClickListener(v -> openFilePicker());

        btnRemoveFile.setOnClickListener(v -> {
            selectedCvUri = null;
            CvManager.getInstance().clearCv();
            layoutFileSelected.setVisibility(View.GONE);
            layoutUploadZone.setVisibility(View.VISIBLE);
        });

        btnSubmit.setOnClickListener(v -> submitApplication());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(
                Intent.createChooser(intent, "Sélectionner votre CV (PDF)"),
                REQUEST_CV
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CV && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String mimeType = getContentResolver().getType(uri);
            if ("application/pdf".equals(mimeType)) {
                selectedCvUri = uri;
                CvManager.getInstance().setCvUri(uri);
                showFileSelected(uri);
            } else {
                Toast.makeText(this, getString(R.string.pdf_only_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showFileSelected(Uri uri) {
        layoutUploadZone.setVisibility(View.GONE);
        layoutFileSelected.setVisibility(View.VISIBLE);

        String name = "cv.pdf";
        long size = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIdx >= 0) name = cursor.getString(nameIdx);
                if (sizeIdx >= 0) size = cursor.getLong(sizeIdx);
            }
        } catch (Exception ignored) {}

        tvFileName.setText(name);
        tvFileSize.setText(String.format("%.1f KB", size / 1024.0));
    }

    private void submitApplication() {
        if (selectedCvUri == null) {
            Toast.makeText(this, "Veuillez importer votre CV", Toast.LENGTH_SHORT).show();
            return;
        }
        // ✅ Signaler à JobDetailActivity que le CV est prêt
        // puis fermer → onActivityResult() dans JobDetailActivity lancera l'analyse
        setResult(RESULT_OK);
        finish();
    }

    private JobOffer findJobById(int id) {
        for (JobOffer j : SampleData.getSampleJobs()) {
            if (j.getId() == id) return j;
        }
        return null;
    }
}
