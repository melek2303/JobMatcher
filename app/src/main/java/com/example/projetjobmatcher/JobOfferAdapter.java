package com.example.projetjobmatcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView Adapter for job offer cards.
 * Mirrors the map() rendering logic from App.tsx.
 */
public class JobOfferAdapter extends RecyclerView.Adapter<JobOfferAdapter.ViewHolder> {

    // ── Callback interfaces ──────────────────────────────────────────────────

    public interface OnViewDetailsListener {
        void onViewDetails(JobOffer job);
    }

    public interface OnBookmarkListener {
        void onBookmark(JobOffer job, int position);
    }

    // ── Fields ───────────────────────────────────────────────────────────────

    private List<JobOffer> jobs;
    private final Context context;
    private OnViewDetailsListener viewDetailsListener;
    private OnBookmarkListener bookmarkListener;

    // ── Constructor ──────────────────────────────────────────────────────────

    public JobOfferAdapter(Context context, List<JobOffer> jobs) {
        this.context = context;
        this.jobs = jobs;
    }

    public void setOnViewDetailsListener(OnViewDetailsListener l) { this.viewDetailsListener = l; }
    public void setOnBookmarkListener(OnBookmarkListener l)        { this.bookmarkListener = l; }

    // ── RecyclerView.Adapter overrides ───────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_job_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JobOffer job = jobs.get(position);

        holder.tvTitle.setText(job.getTitle());
        holder.tvCompany.setText(job.getCompany());
        holder.tvLocation.setText(job.getLocation());
        holder.tvContractType.setText(job.getContractType());
        holder.tvPostedDate.setText(job.getPostedDate());

        // Show salary only when available
        if (job.hasSalary()) {
            holder.rowSalary.setVisibility(View.VISIBLE);
            holder.tvSalary.setText(job.getSalary());
        } else {
            holder.rowSalary.setVisibility(View.GONE);
        }

        // Bookmark tint: accent_blue when saved, muted otherwise
        int tintColor = job.isSaved()
                ? ContextCompat.getColor(context, R.color.accent_blue)
                : ContextCompat.getColor(context, R.color.text_muted);
        holder.btnBookmark.setColorFilter(tintColor);

        // Listeners
        holder.btnViewDetails.setOnClickListener(v -> {
            if (viewDetailsListener != null) viewDetailsListener.onViewDetails(job);
        });

        holder.btnBookmark.setOnClickListener(v -> {
            if (bookmarkListener != null) bookmarkListener.onBookmark(job, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return jobs.size(); }

    // ── Data update ──────────────────────────────────────────────────────────

    public void updateList(List<JobOffer> newJobs) {
        this.jobs = newJobs;
        notifyDataSetChanged();
    }

    // ── ViewHolder ───────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCompany, tvLocation, tvContractType, tvSalary, tvPostedDate;
        LinearLayout rowSalary;
        ImageButton btnBookmark;
        Button btnViewDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle        = itemView.findViewById(R.id.tvJobTitle);
            tvCompany      = itemView.findViewById(R.id.tvCompany);
            tvLocation     = itemView.findViewById(R.id.tvLocation);
            tvContractType = itemView.findViewById(R.id.tvContractType);
            tvSalary       = itemView.findViewById(R.id.tvSalary);
            tvPostedDate   = itemView.findViewById(R.id.tvPostedDate);
            rowSalary      = itemView.findViewById(R.id.rowSalary);
            btnBookmark    = itemView.findViewById(R.id.btnBookmark);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
