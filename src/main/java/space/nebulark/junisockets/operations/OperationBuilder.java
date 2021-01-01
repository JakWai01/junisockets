package space.nebulark.junisockets.operations;

public class OperationBuilder {

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

    public OperationBuilder setRejected(Boolean rejected) {
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

    public OperationBuilder setSet(Boolean set) {
        this.set = set;
        return this;
    }

    public OperationBuilder setIsConnectionAlias(Boolean isConnectionAlias) {
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

    public Object build() {
        
        if (boundAlias != null && clientAlias != null) {
             return new Accept(boundAlias, clientAlias);
        } else if (id != null && rejected != null) { 
            return new Acknowledgement(id, rejected);
        } else if (id != null && alias != null && clientConnectionId != null && set != null && isConnectionAlias != null) {
            return new Alias(id, alias, set, clientConnectionId, isConnectionAlias);
        } else if (offererId != null && answererId != null && answer != null) {
            return new Answer(offererId, answererId, answer);
        } else if (offererId != null && answererId != null && candidate != null) {
            return new Candidate(offererId, answererId, candidate);
        }  else if (offererId != null && answererId != null && offer != null) {
            return new Offer(offererId, answererId, offer);
        } else if (offererId != null && answererId != null) {
            return new Greeting(offererId, answererId);
        }  else if (id != null && alias != null && set != null && clientConnectionId != null) {
            return new Alias(id, alias, set, clientConnectionId);
        } else if (id != null && alias != null && set != null) {
            return new Alias(id, alias, set);
        } else if (id != null) {
            return new Goodbye(id);
        } else {
            throw new IllegalArgumentException();
        }
    }

}