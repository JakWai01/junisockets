package space.nebulark.junisockets.addresses;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import space.nebulark.junisockets.services.SignalingServer;

public class IPAddressTest {
   
    @Test public void testParseIPAddress() {

        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);

        String ipAddress = "127.0.0.1";

        Assert.assertEquals(4, ip.parseIPAddress(ipAddress).length);
    }

    @Test 
    public void testToIPAddress() {

        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);
    
        String subnet = "127.0.0";
        int suffix = 0;

        Assert.assertEquals("127.0.0.0", ip.toIPAddress(subnet, suffix));
    }

    @Test
    public void testCreateIPAddress() {

        String subnet = "127.0.0";

        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);

        Assert.assertEquals("127.0.0.0", ip.createIPAddress(subnet));

        final String[] partsIPAddress = ip.parseIPAddress("127.0.0.0");

        String suffix = partsIPAddress[3];
        Assert.assertEquals(true, subnets.get(subnet).containsKey(Integer.parseInt(suffix)));
    }
    
    @Test 
    public void testRemoveIPAddress() {

        String subnet = "127.0.0";
        
        Logger logger = Logger.getLogger(SignalingServer.class);
        ReentrantLock mutex = new ReentrantLock();
        ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
        IPAddress ip = new IPAddress(logger, mutex, subnets);

        String ipAddress = ip.createIPAddress(subnet);
    
        final String[] partsIPAddress = ip.parseIPAddress(ipAddress);

        String suffix = partsIPAddress[3];

        ip.removeIPAddress(ipAddress);
        Assert.assertEquals(false, subnets.get(subnet).containsKey(Integer.parseInt(suffix)));
    }
}
