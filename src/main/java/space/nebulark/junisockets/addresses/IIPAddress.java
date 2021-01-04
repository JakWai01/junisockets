package space.nebulark.junisockets.addresses;

/**
 * Defines IP address
 */
public interface IIPAddress {

    /**
     * Creates IP address
     * @param subnet
     * @return String
     */
    String createIPAddress(String subnet);

    /**
     * Removes IP address
     * @param ipAddress
     */
    void removeIPAddress(String ipAddress);

    /**
     * Assembles IP address out of subnet and suffix
     * @param subnet
     * @param suffix
     * @return String
     */
    String toIPAddress(String subnet, int suffix);

    /**
     * Parses IP address into octets (e.g. "127.0.0.1" -> ["127", "0", "0", "1"])
     * @param ipAddress
     * @return String[]
     */
    String[] parseIPAddress(String ipAddress);

}
