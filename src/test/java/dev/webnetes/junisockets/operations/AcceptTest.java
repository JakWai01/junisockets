package dev.webnetes.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see dev.webnetes.junisockets.operations.Accept
 */
public class AcceptTest {
  
    /**
     * @see dev.webnetes.junisockets.operations.Accept#getAsJSON()
     */
    @Test
    public void testGetAsJSON() {
        
        Accept accept = new Accept("127.0.0.1:1234", "127.0.0.2:0");

        Assert.assertEquals("{\"data\":{\"boundAlias\":\"127.0.0.1:1234\",\"clientAlias\":\"127.0.0.2:0\"},\"opcode\":\"accept\"}", accept.getAsJSON(accept));
    }
}
