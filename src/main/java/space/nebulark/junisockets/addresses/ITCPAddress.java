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
     * @param ipAddress IP address of client
     * @return String
     * @throws SuffixDoesNotExist Thrown if suffix does not exist
     * @throws SubnetDoesNotExist Thrown if subnet does not exist
     */
    String createTCPAddress(String ipAddress) throws SuffixDoesNotExist, SubnetDoesNotExist;

    /**
     * Claims TCP address
     * @param tcpAddress TCP address of client
     * @throws PortAlreadyAllocated Throws if port is already allocated
     * @throws SubnetDoesNotExist Thrown if subnet does not exist
     */
    void claimTCPAddress(String tcpAddress) throws PortAlreadyAllocated, SubnetDoesNotExist;

    /**
     * Removes TCP address
     * @param tcpAddress TCP address of client
     */
    void removeTCPAddress(String tcpAddress);

    /**
     * Assembles TCP address out of ipAddress and port
     * @param ipAddress IP Address of client
     * @param port port of client
     * @return String
     */
    String toTCPAddress(String ipAddress, int port);

    /**
     * Parses TCP address into ipAddress and port (e.g. "127.0.0.1:8080" = ["127.0.0.1", "8080"])
     * @param tcpAddress TCP address of client
     * @return String[]
     */
    String[] parseTCPAddress(String tcpAddress);
}
