package dev.webnetes.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see dev.webnetes.junisockets.operations.Acknowledgement
 */
public class AcknowledgementTest {
   
    /**
     * @see dev.webnetes.junisockets.operations.Acknowledgement#getAsJSON()
     */
    @Test 
    public void testGetAsJSON() {
        
        Acknowledgement acknowledgement = new Acknowledgement("127.0.0.1", false);

        Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.1\",\"rejected\":false},\"opcode\":\"acknowledged\"}", acknowledgement.getAsJSON(acknowledgement));
    }
}
