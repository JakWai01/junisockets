package space.nebulark.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see space.nebulark.junisockets.operations.Goodbye
 */
public class GoodbyeTest {
   
    /**
     * @see space.nebulark.junisockets.operations.Goodbye#getAsJSON()
     */
    @Test
    public void testGetAsJSON() {
        
        Goodbye goodbye = new Goodbye("127.0.0.2");

        Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.2\"},\"opcode\":\"goodbye\"}", goodbye.getAsJSON(goodbye));
    }
}
