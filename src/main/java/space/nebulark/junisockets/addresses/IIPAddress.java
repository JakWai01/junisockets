package space.nebulark.junisockets.addresses;

public interface IIPAddress {

    String createIPAddress(String subnet);

    void removeIPAddress(String ipAddress);

    String toIPAddress(String subnet, int suffix);

    String[] parseIPAddress(String ipAddress);

}
