package space.nebulark.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see space.nebulark.junisockets.operations.Acknowledgement
 */
public class AcknowledgementTest {
   
    /**
     * @see space.nebulark.junisockets.operations.Acknowledgement#getAsJSON()
     */
    @Test 
    public void testGetAsJSON() {
        
        Acknowledgement acknowledgement = new Acknowledgement("127.0.0.1", false);

        Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.1\",\"rejected\":false},\"opcode\":\"acknowledged\"}", acknowledgement.getAsJSON(acknowledgement));
    }
}
