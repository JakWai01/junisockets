package space.nebulark.junisockets.operations;

public class OperationFactory {

    // der else kann nicht eintreten
    ESignalingOperationCode opcode;
   

    public OperationFactory(ESignalingOperationCode opcode) {
        this.opcode = opcode;
    }

    private String boundAlias;
    private String clientAlias;
    private String id;
    private Boolean rejected;
    private String alias;
    private String clientConnectionId;
    private Boolean set;
    private Boolean isConnectionAlias;
    private String offererId;
    private String answererId;
    private String answer;
    private String candidate;
    private String offer;

    public OperationFactory setBoundAlias(String boundAlias) {
        this.boundAlias = boundAlias;
        return this;
    }

    public OperationFactory setClientAlias(String clientAlias) {
        this.clientAlias = clientAlias;
        return this;
    }

    public OperationFactory setId(String id) {
        this.id = id;
        return this;
    }

    public OperationFactory setRejected(Boolean rejected) {
        this.rejected = rejected;
        return this;
    }

    public OperationFactory setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public OperationFactory setClientConnectionId(String clientConnectionId) {
        this.clientConnectionId = clientConnectionId;
        return this;
    }

    public OperationFactory setSet(Boolean set) {
        this.set = set;
        return this;
    }

    public OperationFactory setIsConnectionAlias(Boolean isConnectionAlias) {
        this.isConnectionAlias = isConnectionAlias;
        return this;
    }

    public OperationFactory setOffererId(String offererId) {
        this.offererId = offererId;
        return this;
    }

    public OperationFactory setAnswererId(String answererId) {
        this.answererId = answererId;
        return this;
    }

    public OperationFactory setAnswer(String answer) {
        this.answer = answer;
        return this;
    }

    public OperationFactory setCandidate(String candidate) {
        this.candidate = candidate;
        return this;
    }

    public OperationFactory setOffer(String offer) {
        this.offer = offer;
        return this;
    }

    public Object getOperation() {

        if (opcode.getValue().equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
            return new Acknowledgement(id, rejected);
        } else if (opcode.getValue().equals(ESignalingOperationCode.GREETING.getValue())) {
            return new Greeting(offererId, answererId);
        } else if (opcode.getValue().equals(ESignalingOperationCode.OFFER.getValue())) {
            return new Offer(offererId, answererId, offer);
        } else if (opcode.getValue().equals(ESignalingOperationCode.ANSWER.getValue())) {
            return new Answer(offererId, answererId, answer);
        } else if (opcode.getValue().equals(ESignalingOperationCode.CANDIDATE.getValue())) {
            return new Candidate(offererId, answererId, candidate);
        } else if (opcode.getValue().equals(ESignalingOperationCode.ALIAS.getValue())) {
            if (id != null && alias != null && clientConnectionId != null && set != null && isConnectionAlias != null) {
                return new Alias(id, alias, set, clientConnectionId, isConnectionAlias);
            } else if (id != null && alias != null && clientConnectionId != null && set != null) {
                return new Alias(id, alias, set, clientConnectionId);
            } else {
                return new Alias(id, alias, set);
            }
        } else if (opcode.getValue().equals(ESignalingOperationCode.ACCEPT.getValue())) {
            return new Accept(boundAlias, clientAlias);
        } else {
            return new Goodbye(id);
        } 
    }
}
