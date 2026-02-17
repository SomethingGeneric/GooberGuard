package cloud.goober.gooberguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class GooberVpnService extends VpnService {
    private static final String TAG = "GooberVpnService";
    private static final String VPN_ADDRESS = "10.0.0.2";
    private static final String VPN_ROUTE = "0.0.0.0";
    
    private Thread vpnThread;
    private ParcelFileDescriptor vpnInterface;
    private boolean isRunning = false;
    private Set<String> blockedDomains;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting VPN service");
        
        // Load blocked domains from SharedPreferences
        loadBlockedDomains();
        
        if (!isRunning) {
            startVpn();
        }
        
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stopping VPN service");
        stopVpn();
        super.onDestroy();
    }

    private void loadBlockedDomains() {
        SharedPreferences prefs = getSharedPreferences("blocked_domains", MODE_PRIVATE);
        blockedDomains = prefs.getStringSet("domains", new HashSet<>());
        Log.d(TAG, "Loaded " + blockedDomains.size() + " blocked domains");
    }

    private void startVpn() {
        // Configure the VPN interface
        Builder builder = new Builder();
        builder.setMtu(1500);
        builder.addAddress(VPN_ADDRESS, 32);
        builder.addRoute(VPN_ROUTE, 0);
        
        // Set DNS servers (we'll intercept these)
        builder.addDnsServer("8.8.8.8");
        builder.addDnsServer("8.8.4.4");
        
        // Set the session name
        builder.setSession("GooberGuard");
        builder.setConfigureIntent(null);

        try {
            vpnInterface = builder.establish();
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface");
                return;
            }

            isRunning = true;
            
            // Start the packet processing thread
            vpnThread = new Thread(this::processPackets, "VpnThread");
            vpnThread.start();
            
            Log.d(TAG, "VPN started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error starting VPN", e);
        }
    }

    private void stopVpn() {
        isRunning = false;
        
        if (vpnThread != null) {
            vpnThread.interrupt();
        }
        
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing VPN interface", e);
            }
        }
    }

    private void processPackets() {
        Log.d(TAG, "Starting packet processing");
        
        FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
        FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
        
        ByteBuffer packet = ByteBuffer.allocate(1500);
        int blockedPacketsCount = 0;
        
        while (isRunning) {
            try {
                // Read packet from VPN interface
                int length = in.read(packet.array());
                if (length > 0) {
                    packet.limit(length);
                    
                    // Check if this is a DNS query packet
                    if (DnsPacketParser.isDnsQuery(packet)) {
                        String domain = DnsPacketParser.extractDomainName(packet);
                        
                        if (domain != null && isBlockedDomain(domain)) {
                            Log.d(TAG, "Blocking DNS query for domain: " + domain);
                            blockedPacketsCount++;
                            
                            // Create and send NXDOMAIN response
                            ByteBuffer response = DnsPacketParser.createBlockedResponse(packet);
                            if (response != null) {
                                out.write(response.array(), 0, response.limit());
                                Log.d(TAG, "Sent NXDOMAIN response for: " + domain);
                            }
                        } else {
                            // Forward the packet (allow the DNS query)
                            out.write(packet.array(), 0, length);
                        }
                    } else {
                        // Not a DNS packet, forward it normally
                        out.write(packet.array(), 0, length);
                    }
                    
                    packet.clear();
                }
            } catch (IOException e) {
                if (isRunning) {
                    Log.e(TAG, "Error processing packets", e);
                }
                break;
            }
        }
        
        Log.d(TAG, "Stopped packet processing. Total blocked: " + blockedPacketsCount);
        
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing streams", e);
        }
    }

    /**
     * Check if a domain should be blocked based on the blocked domains list
     * Supports exact matching and subdomain matching
     */
    private boolean isBlockedDomain(String queryDomain) {
        if (queryDomain == null || blockedDomains == null) {
            return false;
        }
        
        // Check each blocked domain
        for (String blockedDomain : blockedDomains) {
            if (DnsPacketParser.matchesBlockedDomain(queryDomain, blockedDomain)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if a domain should be blocked
     * This method can be used by other components to check blocking status
     */
    public boolean isDomainBlocked(String domain) {
        if (blockedDomains == null) {
            loadBlockedDomains();
        }
        return blockedDomains.contains(domain.toLowerCase().trim());
    }

    /**
     * Add a domain to the blocked list and refresh the service
     */
    public void refreshBlockedDomains() {
        loadBlockedDomains();
        Log.d(TAG, "Refreshed blocked domains list: " + blockedDomains.size() + " domains");
    }
}