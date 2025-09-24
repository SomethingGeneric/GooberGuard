package cloud.goober.gooberguard;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DomainManagerTest {

    @Mock
    private Context mockContext;
    
    @Mock
    private SharedPreferences mockPrefs;
    
    @Mock
    private SharedPreferences.Editor mockEditor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putStringSet(anyString(), any(Set.class))).thenReturn(mockEditor);
        when(mockEditor.remove(anyString())).thenReturn(mockEditor);
    }

    @Test
    public void testDefaultDomainsAddedWhenStorageEmpty() {
        // Simulate empty storage
        when(mockPrefs.getStringSet(eq("domains"), any(HashSet.class))).thenReturn(new HashSet<>());
        
        // Create DomainManager - this should trigger default domain addition
        DomainManager domainManager = new DomainManager(mockContext);
        
        // Verify addBlockedDomain was called for both Instagram and Facebook domains
        verify(mockEditor, atLeast(8)).putStringSet(eq("domains"), any(Set.class)); // 4 Instagram + 4 Facebook
        verify(mockEditor, atLeast(8)).apply();
    }

    @Test
    public void testDefaultDomainsNotAddedWhenStorageHasData() {
        // Simulate existing domains in storage
        Set<String> existingDomains = new HashSet<>();
        existingDomains.add("example.com");
        when(mockPrefs.getStringSet(eq("domains"), any(HashSet.class))).thenReturn(existingDomains);
        
        // Create DomainManager - this should NOT trigger default domain addition
        DomainManager domainManager = new DomainManager(mockContext);
        
        // Verify no domains were added since storage already had data
        verify(mockEditor, never()).putStringSet(anyString(), any(Set.class));
    }

    @Test
    public void testAddBlockedDomain() {
        Set<String> existingDomains = new HashSet<>();
        when(mockPrefs.getStringSet(eq("domains"), any(HashSet.class))).thenReturn(existingDomains);
        
        DomainManager domainManager = new DomainManager(mockContext);
        domainManager.addBlockedDomain("test.com");
        
        verify(mockEditor).putStringSet(eq("domains"), any(Set.class));
        verify(mockEditor).apply();
    }
}