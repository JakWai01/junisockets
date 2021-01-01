package space.nebulark.junisockets.operations;

public class OperationBuilder {

    private String boundAlias;
    private String clientAlias;
    private String id;
    private boolean rejected;
    private String alias;
    private String clientConnectionId;
    private boolean set;
    private boolean isConnectionAlias;
    private String offererId;
    private String answererId;
    private String answer;
    private String candidate;
    private String offer;

    public OperationBuilder setBoundAlias(String boundAlias) {
        this.boundAlias = boundAlias;
        return this;
    }

    public OperationBuilder setClientAlias(String clientAlias) {
        this.clientAlias = clientAlias;
        return this;
    }

    public OperationBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public OperationBuilder setRejected(boolean rejected) {
        this.rejected = rejected;
        return this;
    }

    public OperationBuilder setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public OperationBuilder setClientConnectionId(String clientConnectionId) {
        this.clientConnectionId = clientConnectionId;
        return this;
    }

    public OperationBuilder setSet(boolean set) {
        this.set = set;
        return this;
    }

    public OperationBuilder setIsConnectionAlias(boolean isConnectionAlias) {
        this.isConnectionAlias = isConnectionAlias;
        return this;
    }

    public OperationBuilder setOffererId(String offererId) {
        this.offererId = offererId;
        return this;
    }

    public OperationBuilder setAnswererId(String answererId) {
        this.answererId = answererId;
        return this;
    }

    public OperationBuilder setAnswer(String answer) {
        this.answer = answer;
        return this;
    }

    public OperationBuilder setCandidate(String candidate) {
        this.candidate = candidate;
        return this;
    }

    public OperationBuilder setOffer(String offer) {
        this.offer = offer;
        return this;
    }

}