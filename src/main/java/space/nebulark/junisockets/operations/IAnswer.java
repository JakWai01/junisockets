package space.nebulark.junisockets.operations;

/**
 * Defines answer operation
 */
public interface IAnswer extends IOperation {
   
    /**
     * Returns offererId
     * @return String
     */
    String getOffererId();

    /**
     * Returns answererId
     * @return String
     */
    String getAnswererId();

    /**
     * Returns answer
     * @return String
     */
    String getAnswer();

}
