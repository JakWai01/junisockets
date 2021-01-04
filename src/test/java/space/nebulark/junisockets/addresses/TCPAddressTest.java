package space.nebulark.junisockets.addresses;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import space.nebulark.junisockets.errors.PortAlreadyAllocatedError;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;
import space.nebulark.junisockets.services.SignalingServer;

public class TCPAddressTest {

    @Test
    public void testParseTCPAddress() {
        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, subnets, ip);

        String tcpAddress = "127.0.0.0:1234";

        Assert.assertEquals(2, tcp.parseTCPAddress(tcpAddress).length);
        Assert.assertEquals("127.0.0.0", tcp.parseTCPAddress(tcpAddress)[0]);
        Assert.assertEquals("1234", tcp.parseTCPAddress(tcpAddress)[1]);
    }

    @Test
    public void testToTCPAddress() {
        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, subnets, ip);

        String ipAddress = "127.0.0.0";
        int port = 1234;

        Assert.assertEquals("127.0.0.0:1234", tcp.toTCPAddress(ipAddress, port));
    }

    
    /** 
     * @throws SuffixDoesNotExist
     * @throws SubnetDoesNotExist
     */
    @Test
    public void testCreateTCPAddress() throws SuffixDoesNotExist, SubnetDoesNotExist {
        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, subnets, ip);

        String ipAddress = "127.0.0.0";
        String subnet = "127.0.0";

        ip.createIPAddress(subnet);

        Assert.assertEquals("127.0.0.0:0", tcp.createTCPAddress(ipAddress));
    }

    
    /** 
     * @throws SuffixDoesNotExist
     * @throws SubnetDoesNotExist
     */
    @Test(expected = SubnetDoesNotExist.class)
    public void testCreateTCPAddressSubnetDoesNotExist() throws SuffixDoesNotExist, SubnetDoesNotExist {
        Logger logger2 = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex2 = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets2 = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip2 = new IPAddress(logger2, mutex2, subnets2);
        TCPAddress tcp2 = new TCPAddress(logger2, mutex2, subnets2, ip2);
        String ipAddress2 = "127.0.0.0";

        tcp2.createTCPAddress(ipAddress2);
    }

    
    /** 
     * @throws SuffixDoesNotExist
     * @throws SubnetDoesNotExist
     */
    @Test(expected = SuffixDoesNotExist.class) 
    public void testCreateTCPAddressSuffixDoesNotExist() throws SuffixDoesNotExist, SubnetDoesNotExist {
        Logger logger2 = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex2 = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets2 = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip2 = new IPAddress(logger2, mutex2, subnets2);
        TCPAddress tcp2 = new TCPAddress(logger2, mutex2, subnets2, ip2);
        String ipAddress2 = "127.0.0.1";

        String subnet = "127.0.0";
        ip2.createIPAddress(subnet);
        tcp2.createTCPAddress(ipAddress2);

    }

    
    /** 
     * @throws PortAlreadyAllocatedError
     * @throws SubnetDoesNotExist
     */
    @Test
    public void testClaimTCPAddress() throws PortAlreadyAllocatedError, SubnetDoesNotExist {
        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, subnets, ip);

        String tcpAddress = "127.0.0.0:0";

        String[] partsTCPAddress = tcp.parseTCPAddress(tcpAddress);
        String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

        String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
        String suffix = partsIPAddress[3];
        
        ip.createIPAddress(subnet);
        tcp.claimTCPAddress(tcpAddress);

        Assert.assertEquals(true, subnets.get(subnet).get(Integer.parseInt(suffix)).contains(Integer.parseInt(partsTCPAddress[1])));
    }

    
    /** 
     * @throws PortAlreadyAllocatedError
     * @throws SubnetDoesNotExist
     */
    @Test(expected = PortAlreadyAllocatedError.class) 
    public void testClaimTCPAddressPortAlreadtAllocated() throws PortAlreadyAllocatedError, SubnetDoesNotExist {
        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, subnets, ip);
        String tcpAddress = "127.0.0.0:0";
        String subnet = "127.0.0";

        ip.createIPAddress(subnet);
        tcp.claimTCPAddress(tcpAddress);
        tcp.claimTCPAddress(tcpAddress);
    }

    
    /** 
     * @throws PortAlreadyAllocatedError
     * @throws SubnetDoesNotExist
     */
    @Test(expected = SubnetDoesNotExist.class)
    public void testClaimTCPAddressSubnetDoesNotExist() throws PortAlreadyAllocatedError, SubnetDoesNotExist {
        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, subnets, ip);

        String tcpAddress = "127.0.0.0:0";
        tcp.claimTCPAddress(tcpAddress);
    }

    
    /** 
     * @throws PortAlreadyAllocatedError
     * @throws SubnetDoesNotExist
     */
    @Test
    public void testRemoveTCPAddress() throws PortAlreadyAllocatedError, SubnetDoesNotExist {
        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, subnets, ip);

        String tcpAddress = "127.0.0.0:0";

        String[] partsTCPAddress = tcp.parseTCPAddress(tcpAddress);
        String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

        String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
        String suffix = partsIPAddress[3];

        ip.createIPAddress(subnet);
        tcp.claimTCPAddress(tcpAddress);

        tcp.removeTCPAddress(tcpAddress);
        
        Assert.assertEquals(false, subnets.get(subnet).get(Integer.parseInt(suffix)).contains(Integer.parseInt(partsTCPAddress[1])));
    }

}
