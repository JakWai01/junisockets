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

    public TCPAddress(Logger logger, ReentrantLock mutex, ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets, IPAddress ip) {
        this.logger = logger;
        this.mutex = mutex;
        this.subnets = subnets;
        this.ip = ip;
    }

    public String createTCPAddress(String ipAddress) throws SuffixDoesNotExist, SubnetDoesNotExist {
        logger.debug("Creating TCP address" + ipAddress);

        mutex.lock();

        try {
            final String[] partsIPAddress = ip.parseIPAddress(ipAddress);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            // parse suffix directly to int
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(Integer.parseInt(suffix))) {

                    // can we sort a list?
                    subnets.get(subnet).get(Integer.parseInt(suffix)).sort((a, b) -> a - b);

                    int newPort = 0;

                    for (int i = 0; i < subnets.get(subnet).get(Integer.parseInt(suffix)).size(); i++) {
                        if (subnets.get(subnet).get(Integer.parseInt(suffix)).get(i) != i) {
                            newPort = i;
                        }
                    }

                    subnets.get(subnet).get(Integer.parseInt(suffix)).add(newPort);

                    return toTCPAddress(ip.toIPAddress(subnet, Integer.parseInt(suffix)), newPort);
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

    public void claimTCPAddress(String tcpAddress) throws PortAlreadyAllocatedError, SubnetDoesNotExist {
        logger.debug("Claiming TCP address" + tcpAddress);

        mutex.lock();

        try {
            final String[] partsTCPAddress = parseTCPAddress(tcpAddress);
            logger.debug("partsTCP " + partsTCPAddress.length);
            final String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);
            logger.debug("partsIP " + partsIPAddress.length);
            List<Integer> member = new ArrayList<Integer>();

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (!(subnets.get(subnet).containsKey(Integer.parseInt(suffix)))) {
                    subnets.get(subnet).put(Integer.parseInt(suffix), member);
                }

                // funktioniert das ? ist das if vielleicht immer true?
                if (subnets.get(subnet).get(Integer.parseInt(suffix)).stream()
                        .filter(e -> e == Integer.parseInt(partsTCPAddress[1])).collect(Collectors.toList())
                        .size() == 0) {
                    subnets.get(subnet).get(Integer.parseInt(suffix)).add(Integer.parseInt(partsTCPAddress[1]));
                    logger.debug("Port added to subnets " + partsTCPAddress[1]);
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

    public void removeTCPAddress(String tcpAddress) {
        logger.debug("Removing TCP address" + tcpAddress);

        mutex.lock();

        try {
            final String[] partsTCPAddress = parseTCPAddress(tcpAddress);
            final String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            logger.debug("before");
            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(Integer.parseInt(suffix))) {
                    subnets.get(subnet).put(Integer.parseInt(suffix),
                            subnets.get(subnet).get(Integer.parseInt(suffix)).stream()
                                    .filter(e -> e != Integer.parseInt(partsTCPAddress[1]))
                                    .collect(Collectors.toList())); // We ensure above
                }
            }
            logger.debug("after");

        } finally {
            mutex.unlock();
        }
    }

    public String toTCPAddress(String ipAddress, int port) {
        logger.debug("Converting to TCP address " + ipAddress + port);

        String tcpAddress = ipAddress + ":" + port;

        return tcpAddress;
    }

    public String[] parseTCPAddress(String tcpAddress) {
        logger.debug("Parsing TCP address " + tcpAddress);

        return tcpAddress.split(":");
    }
}
