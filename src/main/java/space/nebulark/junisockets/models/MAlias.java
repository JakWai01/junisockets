package space.nebulark.junisockets.models;

/**
 * MAlias
 */
public class MAlias {
    private String id;
    private boolean accepting;

    /**
     * Constructor for MAlias
     * @param id
     * @param accepting
     */
    public MAlias(String id, boolean accepting) {
        this.id = id;
        this.accepting = accepting;
    }
    

    /** 
     * Returns id
     * @return String
     */
    public String getId() {
        return id;
    }

    
    /** 
     * Returns accepting
     * @return boolean
     */
    public boolean getAccepting() {
        return accepting;
    }
}
