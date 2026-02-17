package cloud.goober.gooberguard;

import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for parsing DNS packets
 * Handles DNS query and response packet structure
 */
public class DnsPacketParser {
    private static final String TAG = "DnsPacketParser";
    
    // DNS header constants
    private static final int DNS_HEADER_SIZE = 12;
    private static final int DNS_QUERY_FLAG = 0x0100;
    private static final int DNS_RESPONSE_FLAG = 0x8180;
    
    // IP header constants
    private static final int IP_HEADER_MIN_SIZE = 20;
    private static final int UDP_HEADER_SIZE = 8;
    private static final int PROTOCOL_UDP = 17;
    
    /**
     * Check if a packet contains a DNS query
     */
    public static boolean isDnsQuery(ByteBuffer packet) {
        try {
            if (packet.limit() < IP_HEADER_MIN_SIZE + UDP_HEADER_SIZE + DNS_HEADER_SIZE) {
                return false;
            }
            
            packet.position(0);
            
            // Check IP version (IPv4 = 0x45)
            byte versionAndIHL = packet.get(0);
            int version = (versionAndIHL >> 4) & 0x0F;
            if (version != 4) {
                return false; // Only support IPv4 for now
            }
            
            // Check protocol (UDP = 17)
            byte protocol = packet.get(9);
            if (protocol != PROTOCOL_UDP) {
                return false;
            }
            
            // Get IP header length
            int ipHeaderLength = (versionAndIHL & 0x0F) * 4;
            
            // Check destination port (DNS = 53)
            int udpDestPort = packet.getShort(ipHeaderLength + 2) & 0xFFFF;
            if (udpDestPort != 53) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if packet is DNS query", e);
            return false;
        }
    }
    
    /**
     * Extract the domain name from a DNS query packet
     */
    public static String extractDomainName(ByteBuffer packet) {
        try {
            packet.position(0);
            
            // Get IP header length
            byte versionAndIHL = packet.get(0);
            int ipHeaderLength = (versionAndIHL & 0x0F) * 4;
            
            // Skip IP header + UDP header + DNS header to get to the question section
            int dnsQueryStart = ipHeaderLength + UDP_HEADER_SIZE + DNS_HEADER_SIZE;
            
            if (packet.limit() < dnsQueryStart + 1) {
                return null;
            }
            
            packet.position(dnsQueryStart);
            
            // Parse the domain name (QNAME in DNS format)
            StringBuilder domain = new StringBuilder();
            int labelLength;
            
            while ((labelLength = packet.get() & 0xFF) != 0) {
                if (labelLength > 63) {
                    // This might be a pointer (DNS name compression)
                    break;
                }
                
                if (domain.length() > 0) {
                    domain.append('.');
                }
                
                // Read label characters
                for (int i = 0; i < labelLength; i++) {
                    if (!packet.hasRemaining()) {
                        break;
                    }
                    domain.append((char) (packet.get() & 0xFF));
                }
                
                if (!packet.hasRemaining()) {
                    break;
                }
            }
            
            String domainName = domain.toString().toLowerCase();
            if (!domainName.isEmpty()) {
                Log.d(TAG, "Extracted domain: " + domainName);
                return domainName;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting domain name", e);
        }
        
        return null;
    }
    
    /**
     * Create a DNS response packet with NXDOMAIN (domain not found)
     * This blocks the domain by telling the app/browser the domain doesn't exist
     */
    public static ByteBuffer createBlockedResponse(ByteBuffer queryPacket) {
        try {
            queryPacket.position(0);
            ByteBuffer response = ByteBuffer.allocate(queryPacket.limit());
            
            // Copy the query packet
            byte[] queryData = new byte[queryPacket.limit()];
            queryPacket.get(queryData);
            response.put(queryData);
            
            // Get IP header length
            response.position(0);
            byte versionAndIHL = response.get(0);
            int ipHeaderLength = (versionAndIHL & 0x0F) * 4;
            
            // Swap source and destination IP addresses
            for (int i = 0; i < 4; i++) {
                byte srcByte = response.get(12 + i);
                byte dstByte = response.get(16 + i);
                response.put(12 + i, dstByte);
                response.put(16 + i, srcByte);
            }
            
            // Swap source and destination UDP ports
            short srcPort = response.getShort(ipHeaderLength);
            short dstPort = response.getShort(ipHeaderLength + 2);
            response.putShort(ipHeaderLength, dstPort);
            response.putShort(ipHeaderLength + 2, srcPort);
            
            // Modify DNS header for NXDOMAIN response
            int dnsHeaderStart = ipHeaderLength + UDP_HEADER_SIZE;
            
            // Set QR (query/response) bit and RCODE to NXDOMAIN (3)
            // Flags: QR=1 (response), OPCODE=0, AA=0, TC=0, RD=1, RA=1, Z=0, RCODE=3 (NXDOMAIN)
            response.putShort(dnsHeaderStart + 2, (short) 0x8183);
            
            // Update IP header checksum (set to 0 and let system recalculate)
            response.putShort(10, (short) 0);
            
            // Update UDP checksum (set to 0, which is valid for IPv4)
            response.putShort(ipHeaderLength + 6, (short) 0);
            
            response.position(0);
            response.limit(queryPacket.limit());
            
            return response;
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating blocked response", e);
            return null;
        }
    }
    
    /**
     * Check if a domain matches any entry in the blocked list
     * Supports exact match and subdomain matching
     */
    public static boolean matchesBlockedDomain(String queryDomain, String blockedDomain) {
        if (queryDomain == null || blockedDomain == null) {
            return false;
        }
        
        queryDomain = queryDomain.toLowerCase().trim();
        blockedDomain = blockedDomain.toLowerCase().trim();
        
        // Exact match
        if (queryDomain.equals(blockedDomain)) {
            return true;
        }
        
        // Subdomain match (e.g., api.instagram.com matches instagram.com)
        if (queryDomain.endsWith("." + blockedDomain)) {
            return true;
        }
        
        return false;
    }
}
