package space.nebulark.junisockets.addresses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

/**
 * IP address
 */
public class IPAddress implements IIPAddress {

    private Logger logger;
    private ReentrantLock mutex;
    private ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets;

    /**
     * Constructor IPAddress
     * @param logger logger
     * @param mutex mutex
     * @param subnets subnets
     */
    public IPAddress(Logger logger, ReentrantLock mutex,
            ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets) {
        this.logger = logger;
        this.mutex = mutex;
        this.subnets = subnets;
    }

    
    /** 
     * Creates IP address 
     * @param subnet subnet of client
     * @return String
     */
    public String createIPAddress(String subnet) {
        logger.trace("Creating IP address " + subnet);

        mutex.lock();

        try {
            // Check if the given subnet is in subnets
            if (!subnets.containsKey(subnet)) {
                subnets.put(subnet, new HashMap<Integer, List<Integer>>());
            }

            List<Integer> existingMembersSorted = subnets.get(subnet).keySet().stream().collect(Collectors.toList());
            
            Collections.sort(existingMembersSorted, (o1, o2) -> o1.compareTo(o2));

            boolean foundSuffix = false;
            int newSuffix = 0;

            // Find free suffix for given subnet
            for (int i = 0; i < existingMembersSorted.size(); i++) {
                if (i != existingMembersSorted.get(i)) {
                    newSuffix = i;
                    foundSuffix = true;

                    break;
                }
            }

            // If there is no free suffix in between other suffixes, extend the number of suffixes by one
            if (!foundSuffix) {
                newSuffix = existingMembersSorted.size();
            }

            // Return -1 if there are more than 255 suffixes used
            if (newSuffix > 255) {
                return "-1";
            }

            List<Integer> newMember = new ArrayList<Integer>();

            subnets.get(subnet).put(newSuffix, newMember); // We ensure above

            return toIPAddress(subnet, newSuffix);

        } finally {
            mutex.unlock();
        }
    }

    
    /** 
     * Removes IP Address
     * @param ipAddress IP address of client
     */
    public void removeIPAddress(String ipAddress) {
        logger.trace("Removing IP address " + ipAddress);

        mutex.lock();

        try {
            final String[] partsIPAddress = parseIPAddress(ipAddress);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            int suffix = Integer.parseInt(partsIPAddress[3]);

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(suffix)) {
                    // remove the suffix so that the given IP is available again
                    subnets.get(subnet).remove(suffix); // We ensure above
                }
            }
        } finally {
            mutex.unlock();
        }
    }

    
    /** 
     * Assembles IP address out of subnet and suffix
     * @param subnet subnet of client
     * @param suffix suffix of client
     * @return String
     */
    public String toIPAddress(String subnet, int suffix) {
        logger.trace("Converting to IP address " + subnet + "." + suffix);

        String ipAddress = subnet + "." + suffix;

        return ipAddress;
    }

    
    /** 
     * Parses IP address into octets (e.g. "127.0.0.1" = ["127", "0", "0", "1"])
     * @param ipAddress IP Address of client
     * @return String[]
     */
    public String[] parseIPAddress(String ipAddress) {
        logger.trace("Parsing IP address " + ipAddress);

        return ipAddress.split(Pattern.quote("."));
    }
}
