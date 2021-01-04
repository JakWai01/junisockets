package space.nebulark.junisockets.addresses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import space.nebulark.junisockets.errors.PortAlreadyAllocatedError;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;

public class TCPAddress implements ITCPAddress {

    Logger logger;
    ReentrantLock mutex;
    ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets;
    IPAddress ip;

    public TCPAddress(Logger logger, ReentrantLock mutex,
            ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets, IPAddress ip) {
        this.logger = logger;
        this.mutex = mutex;
        this.subnets = subnets;
        this.ip = ip;
    }

    
    /** 
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
     * @param tcpAddress
     * @throws PortAlreadyAllocatedError
     * @throws SubnetDoesNotExist
     */
    public void claimTCPAddress(String tcpAddress) throws PortAlreadyAllocatedError, SubnetDoesNotExist {
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
                    throw new PortAlreadyAllocatedError();
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
     * @param tcpAddress
     * @return String[]
     */
    public String[] parseTCPAddress(String tcpAddress) {
        logger.trace("Parsing TCP address " + tcpAddress);

        return tcpAddress.split(":");
    }
}
