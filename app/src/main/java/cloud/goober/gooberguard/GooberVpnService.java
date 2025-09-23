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
        
        while (isRunning) {
            try {
                // Read packet from VPN interface
                int length = in.read(packet.array());
                if (length > 0) {
                    packet.limit(length);
                    
                    // Process the packet (simplified DNS filtering)
                    if (shouldBlockPacket(packet)) {
                        Log.d(TAG, "Blocking packet to filtered domain");
                        // Drop the packet by not forwarding it
                        continue;
                    }
                    
                    // For now, we'll implement basic DNS blocking
                    // In a full implementation, you'd parse UDP/DNS packets
                    // and respond with NXDOMAIN for blocked domains
                    
                    packet.clear();
                }
            } catch (IOException e) {
                if (isRunning) {
                    Log.e(TAG, "Error processing packets", e);
                }
                break;
            }
        }
        
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing streams", e);
        }
    }

    private boolean shouldBlockPacket(ByteBuffer packet) {
        // Simplified check - in reality you'd parse the DNS query
        // and check if the domain is in the blocked list
        // This is a placeholder implementation
        
        // For now, we'll use a simple heuristic based on common Instagram domains
        if (blockedDomains.contains("instagram.com") || 
            blockedDomains.contains("www.instagram.com")) {
            // In a real implementation, you'd parse the DNS query
            // to extract the domain name and check against the blocked list
            return true;
        }
        
        return false;
    }
}