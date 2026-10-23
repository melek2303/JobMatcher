package com.example.projetjobmatcher;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity – the Job Offers list screen.
 *
 * Responsibilities mirror the main return block of App.tsx:
 *   • Header with greeting + offer count + Import CV button
 *   • Search bar (keyword filter)
 *   • Filter chips → opens FilterDialog (location + category)
 *   • RecyclerView of job cards via JobOfferAdapter
 *   • Bottom navigation bar
 */
public class MainActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private EditText etSearch;
    private RecyclerView rvJobOffers;
    private TextView tvOfferCount;
    private TextView tvEmpty;
    private TextView chipLocation;
    private TextView chipCategory;
    private TextView chipContract;
    private Button btnImportCv;
    private BottomNavigationView bottomNav;

    // ── Data & adapter ────────────────────────────────────────────────────────
    private List<JobOffer> allJobs;
    private List<JobOffer> filteredJobs;
    private JobOfferAdapter adapter;

    // ── Active filter state ───────────────────────────────────────────────────
    private String activeLocation = "Toutes les villes";
    private String activeCategory = "Toutes les catégories";

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupData();
        setupRecyclerView();
        setupSearchBar();
        setupFilterChips();
        setupImportCvButton();
        setupBottomNav();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserName(); // ← appelé à chaque retour sur MainActivity
    }

    // ── Bind views ────────────────────────────────────────────────────────────

    private void bindViews() {
        etSearch     = findViewById(R.id.etSearch);
        etSearch.setHintTextColor(ContextCompat.getColor(this, R.color.text_muted));
        rvJobOffers  = findViewById(R.id.rvJobOffers);
        tvOfferCount = findViewById(R.id.tvOfferCount);
        tvEmpty      = findViewById(R.id.tvEmpty);
        chipLocation = findViewById(R.id.chipLocation);
        chipCategory = findViewById(R.id.chipCategory);
        chipContract = findViewById(R.id.chipContract);
        btnImportCv  = findViewById(R.id.btnImportCv);
        bottomNav    = findViewById(R.id.bottomNav);
    }
    private void loadUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("nom");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String nom = snapshot.getValue(String.class);
                if (nom != null) {
                    TextView tvGreeting = findViewById(R.id.tvGreeting);
                    tvGreeting.setText("Salut " + nom + "!");
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // ── Data setup ────────────────────────────────────────────────────────────

    private void setupData() {
        allJobs      = SampleData.getSampleJobs();
        filteredJobs = new ArrayList<>(allJobs);
        updateOfferCount();
    }

    // ── RecyclerView setup ────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new JobOfferAdapter(this, filteredJobs);

        adapter.setOnViewDetailsListener(job -> {
            Intent intent = new Intent(this, JobDetailActivity.class);
            intent.putExtra(JobDetailActivity.EXTRA_JOB_ID, job.getId());
            startActivity(intent);
        });

        adapter.setOnBookmarkListener((job, position) -> {
            job.setSaved(!job.isSaved());
            adapter.notifyItemChanged(position);
            String msg = job.isSaved() ? "Offre sauvegardée" : "Offre retirée";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        rvJobOffers.setLayoutManager(new LinearLayoutManager(this));
        rvJobOffers.setAdapter(adapter);
        rvJobOffers.setNestedScrollingEnabled(false);
    }

    // ── Search bar ────────────────────────────────────────────────────────────

    private void setupSearchBar() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ── Filter chips ──────────────────────────────────────────────────────────

    private void setupFilterChips() {
        chipLocation.setOnClickListener(v -> showFilterDialog(true));
        chipCategory.setOnClickListener(v -> showFilterDialog(false));
        chipContract.setOnClickListener(v -> showFilterDialog(false));
    }

    /**
     * Shows a bottom-sheet-style Dialog with Location + Category spinners.
     * @param focusLocation true → pre-select the Location spinner tab
     */
    private void showFilterDialog(boolean focusLocation) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_filter);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // ── Location spinner ──────────────────────────────────────────────
        Spinner spinnerLocation = dialog.findViewById(R.id.spinnerLocation);
        String[] locations = SampleData.getLocations();
        ArrayAdapter<String> locAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, locations);
        locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locAdapter);
        // Pre-select current value
        for (int i = 0; i < locations.length; i++) {
            if (locations[i].equals(activeLocation)) { spinnerLocation.setSelection(i); break; }
        }

        // ── Category spinner ──────────────────────────────────────────────
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        String[] categories = SampleData.getCategories();
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(activeCategory)) { spinnerCategory.setSelection(i); break; }
        }

        // ── Buttons ───────────────────────────────────────────────────────
        Button btnReset = dialog.findViewById(R.id.btnResetFilters);
        Button btnApply = dialog.findViewById(R.id.btnApplyFilters);

        btnReset.setOnClickListener(v -> {
            activeLocation = "Toutes les villes";
            activeCategory = "Toutes les catégories";
            chipLocation.setText("Localisation");
            chipCategory.setText("Catégorie");
            applyFilters(etSearch.getText().toString());
            dialog.dismiss();
        });

        btnApply.setOnClickListener(v -> {
            activeLocation = spinnerLocation.getSelectedItem().toString();
            activeCategory = spinnerCategory.getSelectedItem().toString();

            // Update chip labels to reflect active filters
            chipLocation.setText(activeLocation.equals("Toutes les villes")
                    ? "Localisation" : " " + activeLocation.split(",")[0]);
            chipCategory.setText(activeCategory.equals("Toutes les catégories")
                    ? " Catégorie" : " " + activeCategory);

            applyFilters(etSearch.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    // ── Filtering logic ───────────────────────────────────────────────────────

    /**
     * Combines keyword search + location + category filters.
     * Mirrors the filteredJobs computation in App.tsx.
     */
    private void applyFilters(String query) {
        String q = query.toLowerCase().trim();
        filteredJobs.clear();

        for (JobOffer job : allJobs) {
            // Keyword match
            boolean matchesKeyword = q.isEmpty()
                    || job.getTitle().toLowerCase().contains(q)
                    || job.getCompany().toLowerCase().contains(q)
                    || job.getLocation().toLowerCase().contains(q)
                    || job.getContractType().toLowerCase().contains(q)
                    || job.getCategory().toLowerCase().contains(q);

            // Location match
            boolean matchesLocation = activeLocation.equals("Toutes les villes")
                    || job.getLocation().equals(activeLocation);

            // Category match
            boolean matchesCategory = activeCategory.equals("Toutes les catégories")
                    || job.getCategory().equals(activeCategory);

            if (matchesKeyword && matchesLocation && matchesCategory) {
                filteredJobs.add(job);
            }
        }

        adapter.updateList(filteredJobs);
        updateOfferCount();

        tvEmpty.setVisibility(filteredJobs.isEmpty() ? View.VISIBLE : View.GONE);
        rvJobOffers.setVisibility(filteredJobs.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateOfferCount() {
        tvOfferCount.setText(filteredJobs != null
                ? filteredJobs.size() + " offres disponibles"
                : allJobs.size() + " offres disponibles");
    }

    // ── Import CV button ──────────────────────────────────────────────────────

    /**
     * Opens the system file picker restricted to PDF files.
     * The selected CV is stored in the app and shown in ApplyActivity.
     */
    private void setupImportCvButton() {
        btnImportCv.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(
                    Intent.createChooser(intent, "Sélectionner votre CV (PDF)"),
                    REQUEST_CV_PICK
            );
        });
    }

    private static final int REQUEST_CV_PICK = 101;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CV_PICK && resultCode == RESULT_OK && data != null) {
            // Store URI globally so ApplyActivity can access it
            CvManager.getInstance().setCvUri(data.getData());
            Toast.makeText(this, "CV importé avec succès ✓", Toast.LENGTH_SHORT).show();
            // Update Import CV button label
            btnImportCv.setText("CV ✓");
        }
    }

    // ── Bottom navigation ─────────────────────────────────────────────────────

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_offers);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_offers)  return true;  // already here
            if (id == R.id.nav_saved)   { showSavedJobs();  return true; }
            if (id == R.id.nav_profile) { showProfile();    return true; }
            return false;
        });
    }

    private void showSavedJobs() {
        // Filter to saved jobs only
        activeLocation = "Toutes les villes";
        activeCategory = "Toutes les catégories";
        etSearch.setText("");
        filteredJobs.clear();
        for (JobOffer j : allJobs) { if (j.isSaved()) filteredJobs.add(j); }
        adapter.updateList(filteredJobs);
        updateOfferCount();
        tvEmpty.setVisibility(filteredJobs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }
}
