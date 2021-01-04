package space.nebulark.junisockets.operations;

/**
 * Defines greeting operation
 */
public interface IGreeting extends IOperation {
   
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

}
