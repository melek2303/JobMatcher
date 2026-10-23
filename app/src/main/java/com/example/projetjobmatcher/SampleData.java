package com.example.projetjobmatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides static sample data that mirrors the mock list in App.tsx.
 *
 * ════════════════════════════════════════════════════════════
 *  RECOMMENDED FREE/FREEMIUM API  →  Adzuna API
 * ════════════════════════════════════════════════════════════
 *  • URL   : https://api.adzuna.com/v1/api/jobs/{country}/search/{page}
 *  • Auth  : app_id + app_key (free tier : 250 req/day)
 *  • Params: what (keyword), where (location), category, results_per_page
 *  • Docs  : https://developer.adzuna.com/
 *
 *  Example call (France, "développeur", Paris) :
 *    GET https://api.adzuna.com/v1/api/jobs/fr/search/1
 *        ?app_id=YOUR_ID&app_key=YOUR_KEY
 *        &what=développeur&where=Paris&results_per_page=20
 *
 *  Alternative freemium option: JSearch (via RapidAPI)
 *  • URL   : https://jsearch.p.rapidapi.com/search
 *  • Params: query, location, num_pages
 *  • Free  : 500 req/month on free tier
 *
 *  To replace sample data below with live API results:
 *  1. Add Retrofit + Gson to build.gradle
 *  2. Create AdzunaApiService interface
 *  3. Call from MainActivity.loadJobsFromApi()
 *  4. Map AdzunaJob DTO → JobOffer model
 * ════════════════════════════════════════════════════════════
 */
public class SampleData {

    public static List<JobOffer> getSampleJobs() {
        List<JobOffer> list = new ArrayList<>();

        list.add(new JobOffer(
                1,
                "Développeur Full Stack",
                "TechCorp",
                "Paris, France",
                "Temps plein",
                "45k - 60k €",
                "Nous recherchons un développeur full stack expérimenté pour rejoindre notre équipe " +
                "dynamique. Vous travaillerez sur des projets variés utilisant React, Node.js et PostgreSQL.",
                "Il y a 2 jours",
                "Développement"
        ));

        list.add(new JobOffer(
                2,
                "Designer UI/UX",
                "Creative Studio",
                "Lyon, France",
                "CDI",
                "40k - 50k €",
                "Créez des expériences utilisateur exceptionnelles pour nos clients internationaux. " +
                "Maîtrise de Figma et d'un portfolio solide indispensables.",
                "Il y a 3 jours",
                "Design"
        ));

        list.add(new JobOffer(
                3,
                "Chef de Projet Digital",
                "Marketing Plus",
                "Marseille, France",
                "Temps plein",
                "50k - 65k €",
                "Pilotez des projets digitaux innovants dans un environnement dynamique. " +
                "Vous coordonnerez les équipes techniques et créatives.",
                "Il y a 5 jours",
                "Management"
        ));

        list.add(new JobOffer(
                4,
                "Data Analyst",
                "DataViz Inc",
                "Bordeaux, France",
                "CDI",
                "42k - 55k €",
                "Analysez et visualisez des données complexes pour aider à la prise de décision stratégique. " +
                "Maîtrise SQL, Python et Power BI souhaitée.",
                "Il y a 1 semaine",
                "Data"
        ));

        list.add(new JobOffer(
                5,
                "Développeur Mobile",
                "AppFactory",
                "Toulouse, France",
                "Temps plein",
                "48k - 62k €",
                "Développez des applications mobiles innovantes pour iOS et Android. " +
                "Expérience en Flutter ou React Native appréciée.",
                "Il y a 1 semaine",
                "Développement"
        ));

        list.add(new JobOffer(
                6,
                "DevOps Engineer",
                "CloudSys",
                "Paris, France",
                "CDI",
                "55k - 75k €",
                "Gérez l'infrastructure cloud et les pipelines CI/CD. AWS, Kubernetes et Terraform requis.",
                "Il y a 2 jours",
                "DevOps"
        ));

        list.add(new JobOffer(
                7,
                "Product Manager",
                "StartupHub",
                "Lyon, France",
                "CDI",
                null, // salary not disclosed
                "Définissez la vision produit et collaborez avec les équipes techniques et design " +
                "pour livrer des features à fort impact utilisateur.",
                "Il y a 4 jours",
                "Management"
        ));

        return list;
    }

    /** Distinct cities extracted from sample data (used for location filter spinner). */
    public static String[] getLocations() {
        return new String[]{
                "Toutes les villes",
                "Paris, France",
                "Lyon, France",
                "Marseille, France",
                "Bordeaux, France",
                "Toulouse, France"
        };
    }

    /** Categories extracted from sample data (used for category filter spinner). */
    public static String[] getCategories() {
        return new String[]{
                "Toutes les catégories",
                "Développement",
                "Design",
                "Data",
                "DevOps",
                "Management"
        };
    }
}
