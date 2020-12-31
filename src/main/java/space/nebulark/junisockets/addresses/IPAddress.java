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

public class IPAddress implements IIPAddress{
    
    Logger logger;
    ReentrantLock mutex;
    ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets;
    
    public IPAddress(Logger logger, ReentrantLock mutex, ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets) {
        this.logger = logger;
        this.mutex = mutex;
        this.subnets = subnets;
    }

    public String createIPAddress(String subnet) {
        logger.trace("Creating IP address " + subnet);

        mutex.lock();

        try {

            if (!subnets.containsKey(subnet)) {
                subnets.put(subnet, new HashMap<Integer, List<Integer>>());
            }

            List<Integer> existingMembersSorted = subnets.get(subnet).keySet().stream().collect(Collectors.toList());

            Collections.sort(existingMembersSorted, (o1, o2) -> o1.compareTo(o2));

            boolean foundSuffix = false;
            int newSuffix = 0;

            for (int i = 0; i < existingMembersSorted.size(); i++) {
                if (i != existingMembersSorted.get(i)) {
                    newSuffix = i;
                    foundSuffix = true;
                }
            }

            if (!foundSuffix) {
                newSuffix = existingMembersSorted.size();
            }

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

    public void removeIPAddress(String ipAddress) {
        logger.trace("Removing IP address " + ipAddress);

        mutex.lock();

        try {
            final String[] partsIPAddress = parseIPAddress(ipAddress);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(Integer.parseInt(suffix))) {
                    subnets.get(subnet).remove(Integer.parseInt(suffix)); // We ensure above
                }
            }
        } finally {
            mutex.unlock();
        }
    }

    public String toIPAddress(String subnet, int suffix) {
        logger.trace("Converting to IP address " + subnet + "." + suffix);

        String ipAddress = subnet + "." + suffix;

        return ipAddress;
    }

    public String[] parseIPAddress(String ipAddress) {
        logger.trace("Parsing IP address " + ipAddress);

        return ipAddress.split(Pattern.quote("."));
    }
}
