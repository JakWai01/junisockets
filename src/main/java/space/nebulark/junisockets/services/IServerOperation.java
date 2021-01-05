package space.nebulark.junisockets.services;

import org.java_websocket.WebSocket;
import org.json.simple.JSONObject;

import space.nebulark.junisockets.errors.ClientClosed;
import space.nebulark.junisockets.errors.PortAlreadyAllocated;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;
import space.nebulark.junisockets.operations.Goodbye;
import space.nebulark.junisockets.operations.IOperation;

/**
 * Defines ServerOperation
 */
public interface IServerOperation {
    
    /**
     * Handle client knock
     * @param data data
     * @param conn conn
     */
    void handleKnock(JSONObject data, WebSocket conn);

    /**
     * Handle client offer
     * @param data data
     */
    void handleOffer(JSONObject data);

    /**
     * Handle client answer
     * @param data data
     */
    void handleAnswer(JSONObject data);

    /**
     * Handle client candidate
     * @param data data
     */
    void handleCandidate(JSONObject data);

    /**
     * Handle client bind
     * @param data data
     */
    void handleBind(JSONObject data) throws PortAlreadyAllocated, SubnetDoesNotExist;

    /**
     * Handle client accepting
     * @param data data
     */
    void handleAccepting(JSONObject data);

    /**
     * Handle client shutdown
     * @param data data
     */
    void handleShutdown(JSONObject data);

    /**
     * Handle client connect
     * @param data data
     * @throws SuffixDoesNotExist Thrown if suffix does not exist
     * @throws SubnetDoesNotExist Thrown if subnet does not exist
     */
    void handleConnect(JSONObject data) throws SuffixDoesNotExist, SubnetDoesNotExist;

    /**
     * Send goodbye from leaving client to all remaining clients
     * @param operation operation
     */
    void send(Goodbye operation);

    /**
     * Send response to client. E depends on which handler is sending. E might be one of the operations implementing the IOperation interface.
     * @param conn conn
     * @param operation operation
     * @throws ClientClosed Thrown if client is closed
     */
    <E extends IOperation> void send(WebSocket conn, E operation) throws ClientClosed;

}
