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

    private Logger logger;
    private ReentrantLock mutex;
    private ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets;
    private IPAddress ip;

    /**
     * Constructor TCPAddress
     * @param logger logger
     * @param mutex mutex
     * @param subnets subnets
     * @param ip ip
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
     * @param ipAddress IP adress of client
     * @return String
     * @throws SuffixDoesNotExist Thrown if suffix does not exist
     * @throws SubnetDoesNotExist Thrown if subnet does not exist
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
     * @param tcpAddress TCP address of client
     * @throws PortAlreadyAllocated Thrown if port is already allocated
     * @throws SubnetDoesNotExist Thrown if subnet does not exist
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
     * @param tcpAddress TCP address of client
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
     * @param ipAddress IP address of client
     * @param port port of client
     * @return String
     */
    public String toTCPAddress(String ipAddress, int port) {
        logger.trace("Converting to TCP address " + ipAddress + port);

        String tcpAddress = ipAddress + ":" + port;

        return tcpAddress;
    }
 
    
    /** 
     * Parses TCP address into ipAddress and port (e.g. "127.0.0.1:8080" = ["127.0.0.1", "8080"])
     * @param tcpAddress TCP address of client
     * @return String[]
     */
    public String[] parseTCPAddress(String tcpAddress) {
        logger.trace("Parsing TCP address " + tcpAddress);

        return tcpAddress.split(":");
    }
}
