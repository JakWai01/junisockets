package space.nebulark.junisockets.operations;

public class Offer {
    public ESignalingOperationCode opcode = ESignalingOperationCode.OFFER;

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
