package space.nebulark.junisockets.operations;

import org.java_websocket.WebSocket;
import org.json.simple.JSONObject;

import space.nebulark.junisockets.errors.ClientClosed;
import space.nebulark.junisockets.errors.PortAlreadyAllocatedError;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;

public interface IServerOperation {
   
    void handleKnock(JSONObject data, WebSocket conn);

    void handleOffer(JSONObject data);

    void handleAnswer(JSONObject data);

    void handleCandidate(JSONObject data);

    void handleBind(JSONObject data) throws PortAlreadyAllocatedError, SubnetDoesNotExist;

    void handleAccepting(JSONObject data);

    void handleShutdown(JSONObject data);

    void handleConnect(JSONObject data) throws SuffixDoesNotExist, SubnetDoesNotExist;

    void send(Goodbye operation);

    <E extends IOperation> void send(WebSocket conn, E operation) throws ClientClosed;

}
