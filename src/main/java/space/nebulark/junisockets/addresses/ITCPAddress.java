package space.nebulark.junisockets.addresses;

import space.nebulark.junisockets.errors.PortAlreadyAllocatedError;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;

public interface ITCPAddress {
    
    String createTCPAddress(String ipAddress) throws SuffixDoesNotExist, SubnetDoesNotExist;

    void claimTCPAddress(String tcpAddress) throws PortAlreadyAllocatedError, SubnetDoesNotExist;

    void removeTCPAddress(String tcpAddress);

    String toTCPAddress(String ipAddress, int port);

    String[] parseTCPAddress(String tcpAddress);
}
