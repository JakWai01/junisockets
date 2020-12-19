package space.nebulark.junisockets;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

public class SignalingService {
    Logger logger = Logger.getLogger(SignalingService.class);

    private void send(WebSocket conn) {
        // add operation
        logger.debug("Sending");

        if (conn != null) {
            // transform object to json in here
            conn.send("Hallo");
        } else {
            // throw new custom error
        }
    }


    private void receive(String message) {

        // transform json to string here

        // Add operation code here
        logger.debug("Received operation: " + message);

        if (message.equals(ESignalingOperationCode.KNOCK.getValue())) {
            logger.trace("Received operation knock" + message);

            // return new Knock(operation.data as IKnockData);
        } else if (message.equals(ESignalingOperationCode.OFFER.getValue())) {
            logger.trace("Received operation offer" + message);

            // return new Offer(operation.data as IOfferData);
        } else if (message.equals(ESignalingOperationCode.ANSWER.getValue())) {
            logger.trace("Received operation answer" + message);

            // return new Answer(operation.data as IAnswerData);
        } else if (message.equals(ESignalingOperationCode.CANDIDATE.getValue())) {
            logger.trace("Received operation candidate" + message);

            // return new Candidate(operation.data as ICandidateData);
        } else if (message.equals(ESignalingOperationCode.BIND.getValue())) {
            logger.trace("Received operation bind" + message);

            // return new Bind(operation.data as IBindData);
        } else if (message.equals(ESignalingOperationCode.ACCEPTING.getValue())) {
            logger.trace("Received operation accepting" + message);

            // return new Accepting(operation.data as IAcceptingData);
        } else if (message.equals(ESignalingOperationCode.SHUTDOWN.getValue())) {
            logger.trace("Received operation shutdown" + message);

            // return new Shutdown(operation.data as IShutdownData);
        } else if (message.equals(ESignalingOperationCode.CONNECT.getValue())) {
            logger.trace("Received operation connect" + message);

            // return new Connect(operation.data as IConnectData);
        } else {
            // throw new custom exception
        }
    }
}
