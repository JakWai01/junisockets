package space.nebulark.junisockets.addresses;

import space.nebulark.junisockets.errors.PortAlreadyAllocated;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;

/**
 * Defines TCP address
 */
public interface ITCPAddress {

    /**
     * Creates TCP address
     * @param ipAddress
     * @return String
     * @throws SuffixDoesNotExist
     * @throws SubnetDoesNotExist
     */
    String createTCPAddress(String ipAddress) throws SuffixDoesNotExist, SubnetDoesNotExist;

    /**
     * Claims TCP address
     * @param tcpAddress
     * @throws PortAlreadyAllocatedError
     * @throws SubnetDoesNotExist
     */
    void claimTCPAddress(String tcpAddress) throws PortAlreadyAllocated, SubnetDoesNotExist;

    /**
     * Removes TCP address
     * @param tcpAddress
     */
    void removeTCPAddress(String tcpAddress);

    /**
     * Assembles TCP address out of ipAddress and port
     * @param ipAddress
     * @param port
     * @return String
     */
    String toTCPAddress(String ipAddress, int port);

    /**
     * Parses TCP address into ipAddress and port (e.g. "127.0.0.1:8080" -> ["127.0.0.1", "8080"])
     * @param tcpAddress
     * @return String[]
     */
    String[] parseTCPAddress(String tcpAddress);
}
