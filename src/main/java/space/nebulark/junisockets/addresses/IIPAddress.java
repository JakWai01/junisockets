package space.nebulark.junisockets.addresses;

/**
 * Defines IP address
 */
public interface IIPAddress {

    /**
     * Creates IP address
     * @param subnet subnet of client
     * @return String 
     */
    String createIPAddress(String subnet);

    /**
     * Removes IP address
     * @param ipAddress IP address of client
     */
    void removeIPAddress(String ipAddress);

    /**
     * Assembles IP address out of subnet and suffix
     * @param subnet subnet of client
     * @param suffix suffix of client
     * @return String
     */
    String toIPAddress(String subnet, int suffix);

    /**
     * Parses IP address into octets (e.g. "127.0.0.1" = ["127", "0", "0", "1"])
     * @param ipAddress IP address of client
     * @return String[]
     */
    String[] parseIPAddress(String ipAddress);

}
