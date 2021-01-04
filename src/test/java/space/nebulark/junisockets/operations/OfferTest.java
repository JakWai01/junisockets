package space.nebulark.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see space.nebulark.junisockets.operations.Offer
 */
public class OfferTest {
   
    /**
     * @see space.nebulark.junisockets.operations.Offer#getAsJSON()
     */
    @Test
    public void testGetAsJSON() {

        Offer offer = new Offer("127.0.0.1", "127.0.0.2", "o1");

        Assert.assertEquals("{\"data\":{\"offererId\":\"127.0.0.1\",\"answererId\":\"127.0.0.2\",\"offer\":\"o1\"},\"opcode\":\"offer\"}", offer.getAsJSON(offer));
    }
}
