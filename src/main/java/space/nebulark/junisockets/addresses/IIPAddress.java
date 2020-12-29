package space.nebulark.junisockets.addresses;

public interface IIPAddress {
   
    String createIPAddress(String subnet);

    void removeIPAddress(String ipAddress);

    public String toIPAddress(String subnet, int suffix);

    public String[] parseIPAddress(String ipAddress);
    
}
