package space.nebulark.junisockets.operations;

/**
 * OperationFactory
 */
public class OperationFactory {

    ESignalingOperationCode opcode;

    /**
     * Constructor OperationFactory
     * @param opcode
     */
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

    
    /** 
     * Sets boundAlias
     * @param boundAlias
     * @return OperationFactory
     */
    public OperationFactory setBoundAlias(String boundAlias) {
        this.boundAlias = boundAlias;
        return this;
    }

    
    /** 
     * Sets clientAlias
     * @param clientAlias
     * @return OperationFactory
     */
    public OperationFactory setClientAlias(String clientAlias) {
        this.clientAlias = clientAlias;
        return this;
    }

    
    /** 
     * Sets id
     * @param id
     * @return OperationFactory
     */
    public OperationFactory setId(String id) {
        this.id = id;
        return this;
    }

    
    /** 
     * Sets rejected
     * @param rejected
     * @return OperationFactory
     */
    public OperationFactory setRejected(Boolean rejected) {
        this.rejected = rejected;
        return this;
    }

    
    /** 
     * Sets alias
     * @param alias
     * @return OperationFactory
     */
    public OperationFactory setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    
    /** 
     * Sets clientConnectionId
     * @param clientConnectionId
     * @return OperationFactory
     */
    public OperationFactory setClientConnectionId(String clientConnectionId) {
        this.clientConnectionId = clientConnectionId;
        return this;
    }

    
    /** 
     * Sets set
     * @param set
     * @return OperationFactory
     */
    public OperationFactory setSet(Boolean set) {
        this.set = set;
        return this;
    }

    
    /** 
     * Sets isConnectionAlias
     * @param isConnectionAlias
     * @return OperationFactory
     */
    public OperationFactory setIsConnectionAlias(Boolean isConnectionAlias) {
        this.isConnectionAlias = isConnectionAlias;
        return this;
    }

    
    /** 
     * Sets offererId
     * @param offererId
     * @return OperationFactory
     */
    public OperationFactory setOffererId(String offererId) {
        this.offererId = offererId;
        return this;
    }

    
    /** 
     * Sets answererId
     * @param answererId
     * @return OperationFactory
     */
    public OperationFactory setAnswererId(String answererId) {
        this.answererId = answererId;
        return this;
    }

    
    /** 
     * Sets answer
     * @param answer
     * @return OperationFactory
     */
    public OperationFactory setAnswer(String answer) {
        this.answer = answer;
        return this;
    }

    
    /** 
     * Sets candidate
     * @param candidate
     * @return OperationFactory
     */
    public OperationFactory setCandidate(String candidate) {
        this.candidate = candidate;
        return this;
    }

    
    /** 
     * Sets offer
     * @param offer
     * @return OperationFactory
     */
    public OperationFactory setOffer(String offer) {
        this.offer = offer;
        return this;
    }

    
    /** 
     * Builds the defined object and returns the right operation based on the set parameters
     * @return Object
     */
    public Object getOperation() {

        if (opcode.getValue().equals(ESignalingOperationCode.ACKNOWLEDGED.getValue()) && id != null && rejected != null) {
            return new Acknowledgement(id, rejected);
        } else if (opcode.getValue().equals(ESignalingOperationCode.GREETING.getValue()) && offererId != null && answererId != null) {
            return new Greeting(offererId, answererId);
        } else if (opcode.getValue().equals(ESignalingOperationCode.OFFER.getValue()) && offererId != null && answererId != null && offer != null) {
            return new Offer(offererId, answererId, offer);
        } else if (opcode.getValue().equals(ESignalingOperationCode.ANSWER.getValue()) && offererId != null && answererId != null && answer != null) {
            return new Answer(offererId, answererId, answer);
        } else if (opcode.getValue().equals(ESignalingOperationCode.CANDIDATE.getValue()) && offererId != null && answererId != null && candidate != null) {
            return new Candidate(offererId, answererId, candidate);
        } else if (opcode.getValue().equals(ESignalingOperationCode.ALIAS.getValue())) {
            if (id != null && alias != null && clientConnectionId != null && set != null && isConnectionAlias != null) {
                return new Alias(id, alias, set, clientConnectionId, isConnectionAlias);
            } else if (id != null && alias != null && clientConnectionId != null && set != null) {
                return new Alias(id, alias, set, clientConnectionId);
            } else {
                return new Alias(id, alias, set);
            }
        } else if (opcode.getValue().equals(ESignalingOperationCode.ACCEPT.getValue()) && boundAlias != null && clientAlias != null) {
            return new Accept(boundAlias, clientAlias);
        } else if (opcode.getValue().equals(ESignalingOperationCode.GOODBYE.getValue()) && id != null){
            return new Goodbye(id);
        } else {
            return new IllegalArgumentException();
        }
    }
}
