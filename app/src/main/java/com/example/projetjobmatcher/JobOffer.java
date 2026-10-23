package com.example.projetjobmatcher;

/**
 * Data model representing a single job offer.
 * Maps directly to the JobOffer interface from the original App.tsx.
 */
public class JobOffer {

    private int id;
    private String title;
    private String company;
    private String location;
    private String contractType;   // CDI, Temps plein, Stage, Freelance…
    private String salary;         // nullable – hidden when null/empty
    private String description;
    private String postedDate;
    private String category;       // used for category filter
    private boolean saved;

    public JobOffer(int id, String title, String company, String location,
                    String contractType, String salary, String description,
                    String postedDate, String category) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.contractType = contractType;
        this.salary = salary;
        this.description = description;
        this.postedDate = postedDate;
        this.category = category;
        this.saved = false;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getId()            { return id; }
    public String getTitle()      { return title; }
    public String getCompany()    { return company; }
    public String getLocation()   { return location; }
    public String getContractType() { return contractType; }
    public String getSalary()     { return salary; }
    public String getDescription(){ return description; }
    public String getPostedDate() { return postedDate; }
    public String getCategory()   { return category; }
    public boolean isSaved()      { return saved; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setSaved(boolean saved) { this.saved = saved; }

    public boolean hasSalary() {
        return salary != null && !salary.trim().isEmpty();
    }
}
