package space.nebulark.junisockets.operations;

import org.java_websocket.WebSocket;
import org.json.simple.JSONObject;

import space.nebulark.junisockets.errors.ClientClosed;
import space.nebulark.junisockets.errors.PortAlreadyAllocated;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;

/**
 * Defines ServerOperation
 */
public interface IServerOperation {
    
    /**
     * Handle client knock
     * @param data
     * @param conn
     */
    void handleKnock(JSONObject data, WebSocket conn);

    /**
     * Handle client offer
     * @param data
     */
    void handleOffer(JSONObject data);

    /**
     * Handle client answer
     * @param data
     */
    void handleAnswer(JSONObject data);

    /**
     * Handle client candidate
     * @param data
     */
    void handleCandidate(JSONObject data);

    /**
     * Handle client bind
     */
    void handleBind(JSONObject data) throws PortAlreadyAllocated, SubnetDoesNotExist;

    /**
     * Handle client accepting
     * @param data
     */
    void handleAccepting(JSONObject data);

    /**
     * Handle client shutdown
     * @param data
     */
    void handleShutdown(JSONObject data);

    /**
     * Handle client connect
     * @param data
     * @throws SuffixDoesNotExist
     * @throws SubnetDoesNotExist
     */
    void handleConnect(JSONObject data) throws SuffixDoesNotExist, SubnetDoesNotExist;

    /**
     * Send goodbye from leaving client to all remaining clients
     * @param operation
     */
    void send(Goodbye operation);

    /**
     * Send response to client. E depends on which handler is sending. E might be one of the operations implementing the IOperation interface.
     * @param conn 
     * @param operation
     * @throws ClientClosed
     */
    <E extends IOperation> void send(WebSocket conn, E operation) throws ClientClosed;

}
