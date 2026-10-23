package com.example.projetjobmatcher;

import android.net.Uri;

/**
 * Simple singleton to share the imported CV URI between MainActivity and ApplyActivity.
 * In a production app this would be persisted in SharedPreferences or a ViewModel.
 */
public class CvManager {

    private static CvManager instance;
    private Uri cvUri;

    private CvManager() {}

    public static CvManager getInstance() {
        if (instance == null) instance = new CvManager();
        return instance;
    }

    public Uri getCvUri()           { return cvUri; }
    public void setCvUri(Uri uri)   { this.cvUri = uri; }
    public boolean hasCv()          { return cvUri != null; }
    public void clearCv()           { cvUri = null; }
}
