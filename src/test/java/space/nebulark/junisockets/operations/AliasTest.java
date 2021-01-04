package space.nebulark.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see space.nebulark.junisockets.operations.Alias
 */
public class AliasTest {
   
    /**
     * @see space.nebulark.junisockets.operations.Alias#getAsJSON()
     */
    @Test
    public void testGetAsJSON() {

        Alias alias = new Alias("127.0.0.1", "127.0.0.1:1234", true);

        Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.1\",\"alias\":\"127.0.0.1:1234\",\"set\":true},\"opcode\":\"alias\"}", alias.getAsJSON(alias));

        Alias aliasClientConnectedId = new Alias("127.0.0.1", "127.0.0.1:1234", true,  "co1");

        Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.1\",\"alias\":\"127.0.0.1:1234\",\"set\":true,\"clientConnectionId\":\"co1\"},\"opcode\":\"alias\"}",  aliasClientConnectedId.getAsJSON(aliasClientConnectedId));

        Alias aliasIsConnectionAlias = new Alias("127.0.0.1", "127.0.0.1:1234", true, "co1", true);

        Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.1\",\"alias\":\"127.0.0.1:1234\",\"set\":true,\"clientConnectionId\":\"co1\",\"isConnectionAlias\":true},\"opcode\":\"alias\"}", aliasIsConnectionAlias.getAsJSON(aliasIsConnectionAlias));

    }
}
