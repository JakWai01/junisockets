package space.nebulark.junisockets.addresses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import space.nebulark.junisockets.errors.PortAlreadyAllocated;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;

/**
 * TCP address
 */
public class TCPAddress implements ITCPAddress {

    Logger logger;
    ReentrantLock mutex;
    ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets;
    IPAddress ip;

    /**
     * Constructor TCPAddress
     * @param logger
     * @param mutex
     * @param subnets
     * @param ip
     */
    public TCPAddress(Logger logger, ReentrantLock mutex,
            ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets, IPAddress ip) {
        this.logger = logger;
        this.mutex = mutex;
        this.subnets = subnets;
        this.ip = ip;
    }

    
    /** 
     * Creates TCP address
     * @param ipAddress
     * @return String
     * @throws SuffixDoesNotExist
     * @throws SubnetDoesNotExist
     */
    public String createTCPAddress(String ipAddress) throws SuffixDoesNotExist, SubnetDoesNotExist {
        logger.trace("Creating TCP address " + ipAddress);

        mutex.lock();

        try {
            final String[] partsIPAddress = ip.parseIPAddress(ipAddress);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            int suffix = Integer.parseInt(partsIPAddress[3]);

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(suffix)) {

                    subnets.get(subnet).get(suffix).sort((a, b) -> a - b);

                    int newPort = 0;

                    for (int i = 0; i < subnets.get(subnet).get(suffix).size(); i++) {
                        if (subnets.get(subnet).get(suffix).get(i) != i) {
                            newPort = i;
                        }
                    }

                    subnets.get(subnet).get(suffix).add(newPort);

                    return toTCPAddress(ip.toIPAddress(subnet, suffix), newPort);
                } else {
                    throw new SuffixDoesNotExist();
                }
            } else {
                throw new SubnetDoesNotExist();
            }
        } finally {
            mutex.unlock();
        }
    }
    

    /** 
     * Claims TCP address
     * @param tcpAddress
     * @throws PortAlreadyAllocatedError
     * @throws SubnetDoesNotExist
     */
    public void claimTCPAddress(String tcpAddress) throws PortAlreadyAllocated, SubnetDoesNotExist {
        logger.trace("Claiming TCP address " + tcpAddress);

        mutex.lock();

        try {
            final String[] partsTCPAddress = parseTCPAddress(tcpAddress);
            final String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);
            List<Integer> member = new ArrayList<Integer>();

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            int suffix = Integer.parseInt(partsIPAddress[3]);

            if (subnets.containsKey(subnet)) {
                if (!(subnets.get(subnet).containsKey(suffix))) {
                    subnets.get(subnet).put(suffix, member);
                }

                if (subnets.get(subnet).get(suffix).stream()
                        .filter(e -> e == Integer.parseInt(partsTCPAddress[1])).collect(Collectors.toList())
                        .size() == 0) {
                    subnets.get(subnet).get(suffix).add(Integer.parseInt(partsTCPAddress[1]));
                }

                else {
                    logger.fatal("Port already allocated");
                    throw new PortAlreadyAllocated();
                }
            } else {
                logger.fatal("Subnet does not exist");
                throw new SubnetDoesNotExist();
            }
        } finally {
            mutex.unlock();
        }
    }
    

    /** 
     * Removes TCP address
     * @param tcpAddress
     */
    public void removeTCPAddress(String tcpAddress) {
        logger.trace("Removing TCP address " + tcpAddress);

        mutex.lock();

        try {
            final String[] partsTCPAddress = parseTCPAddress(tcpAddress);
            final String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(Integer.parseInt(suffix))) {
                    subnets.get(subnet).put(Integer.parseInt(suffix),
                            subnets.get(subnet).get(Integer.parseInt(suffix)).stream()
                                    .filter(e -> e != Integer.parseInt(partsTCPAddress[1]))
                                    .collect(Collectors.toList())); // We ensure above
                }
            }

        } finally {
            mutex.unlock();
        }
    }
  
    
    /** 
     * Assembles TCP address out of ipAddress and port
     * @param ipAddress
     * @param port
     * @return String
     */
    public String toTCPAddress(String ipAddress, int port) {
        logger.trace("Converting to TCP address " + ipAddress + port);

        String tcpAddress = ipAddress + ":" + port;

        return tcpAddress;
    }
 
    
    /** 
     * Parses TCP address into ipAddress and port (e.g. "127.0.0.1:8080" -> ["127.0.0.1", "8080"])
     * @param tcpAddress
     * @return String[]
     */
    public String[] parseTCPAddress(String tcpAddress) {
        logger.trace("Parsing TCP address " + tcpAddress);

        return tcpAddress.split(":");
    }
}
