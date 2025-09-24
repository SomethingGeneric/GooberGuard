package cloud.goober.gooberguard;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DomainManager {
    private static final String PREFS_NAME = "blocked_domains";
    private static final String DOMAINS_KEY = "domains";
    
    private SharedPreferences prefs;
    
    public DomainManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Add some default blocked domains if none exist
        if (getBlockedDomains().isEmpty()) {
            addDefaultBlockedDomains();
        }
    }
    
    private void addDefaultBlockedDomains() {
        // Add Instagram and Facebook domains as defaults
        addBlockedDomain("instagram.com");
        addBlockedDomain("www.instagram.com");
        addBlockedDomain("i.instagram.com");
        addBlockedDomain("api.instagram.com");
        
        // Add Facebook domains
        addBlockedDomain("facebook.com");
        addBlockedDomain("www.facebook.com");
        addBlockedDomain("m.facebook.com");
        addBlockedDomain("api.facebook.com");
    }
    
    public ArrayList<String> getBlockedDomains() {
        Set<String> domainSet = prefs.getStringSet(DOMAINS_KEY, new HashSet<>());
        return new ArrayList<>(domainSet);
    }
    
    public void addBlockedDomain(String domain) {
        Set<String> domains = new HashSet<>(prefs.getStringSet(DOMAINS_KEY, new HashSet<>()));
        domains.add(domain.toLowerCase().trim());
        
        prefs.edit()
                .putStringSet(DOMAINS_KEY, domains)
                .apply();
    }
    
    public void removeBlockedDomain(String domain) {
        Set<String> domains = new HashSet<>(prefs.getStringSet(DOMAINS_KEY, new HashSet<>()));
        domains.remove(domain.toLowerCase().trim());
        
        prefs.edit()
                .putStringSet(DOMAINS_KEY, domains)
                .apply();
    }
    
    public boolean isDomainBlocked(String domain) {
        Set<String> domains = prefs.getStringSet(DOMAINS_KEY, new HashSet<>());
        return domains.contains(domain.toLowerCase().trim());
    }
    
    public void clearAllDomains() {
        prefs.edit()
                .remove(DOMAINS_KEY)
                .apply();
    }
}