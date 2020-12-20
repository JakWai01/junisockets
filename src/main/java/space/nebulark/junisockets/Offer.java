package space.nebulark.junisockets;

public class Offer implements ISignalingOperation<IOfferData>{
    ESignalingOperationCode opcode = ESignalingOperationCode.OFFER;

    private String offererId; 
    private String answererId;
    private String offer;

    public Offer(String offererId, String answererId, String offer) {
        this.offererId = offererId;
        this.answererId = answererId;
        this.offer = offer;
    }

    public String getOffererId() {
        return offererId;
    }

    public String getAnswererId() {
        return answererId;
    }

    public String getOffer() {
        return offer;
    }
}
