package com.example.projetjobmatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TextAnalyzer {

    // ces mots apparaissent tres souvent mais ne caracterisent pas un profil

    private static final List<String> STOPWORDS = Arrays.asList(  // ── STOPWORDS : mots a ignorer
            // Francais
            "le","la","les","de","du","des","un","une","et","en",
            "au","aux","pour","par","sur","avec","nous","vous","ils",
            "elles","est","sont","avoir","mais","donc","car","que",
            "qui","dans","plus","tout","bien","comme","meme","aussi",
            "tres","etre","faire","cette","ces","mon","ton","son",

            // Anglais (CV/offres souvent bilingues)
            "the","and","for","with","that","this","have","from",
            "not","are","was","will","your","can","all","been",
            "its","our","has","but","they","you","their","use",
            "used","able","work","good"
    );


    // certains mots cles techniques ont moins de 4 lettres (sql, php, git...)
    // la condition mot.length() > 3 les eliminerait → liste blanche pour les proteger

    private static final List<String> TECH_KEYWORDS = Arrays.asList(  // ── TECH_KEYWORDS : mots techniques courts a toujours garder
            "java","sql","php","css","html","git","api","uml",
            "python","react","node","xml","mvc","ios","android",
            "mysql","linux","agile","scrum","rest","swift","kotlin",
            "docker","spring","angular","vuejs","mongodb"
    );

    public static String cleanText(String texte) {  // ── cleanText() : normalise le texte avant analyse
        return texte.toLowerCase().replaceAll("[^a-zA-Z ]", " "); // replaceAll() : supprime ponctuation et caracteres speciaux
    }


    // fonctionne sur le texte brut du CV (extrait du PDF) ou de l'offre
    public static List<String> extractKeywords(String texte) { // ── extractKeywords() : extrait les mots importants du texte

        String[] mots = cleanText(texte).split("\\s+"); // split("\\s+") : decoupe sur un ou plusieurs espaces
        List<String> keywords = new ArrayList<>();
        for (String mot : mots) {
            if (mot.isEmpty()) continue;

            // cAS 1 : mot technique → toujours garder (meme si < 4 lettres)
            if (TECH_KEYWORDS.contains(mot)) {
                if (!keywords.contains(mot)) keywords.add(mot);
            }

            // cAS 2 : mot ordinaire → garder si > 3 lettres et pas stopword
            else if (mot.length() > 3 && !STOPWORDS.contains(mot)) {
                if (!keywords.contains(mot)) keywords.add(mot);
            }
        }
        return keywords;
    }





    // formule : (mots communs / total mots offre) * 100
    public static int calculateScore(List<String> cv, List<String> offre) {
        if (offre.isEmpty()) return 0; // evite division par zero
        int commun = 0;
        for (String mot : offre) {
            if (cv.contains(mot)) commun++;
        }
        return (int)((commun * 100.0) / offre.size());  // * 100.0 force le calcul decimal
    }
}