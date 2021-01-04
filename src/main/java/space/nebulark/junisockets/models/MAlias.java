package space.nebulark.junisockets.models;

public class MAlias {
    private String id;
    private boolean accepting;

    public MAlias(String id, boolean accepting) {
        this.id = id;
        this.accepting = accepting;
    }

    
    /** 
     * @return String
     */
    public String getId() {
        return id;
    }

    
    /** 
     * @return boolean
     */
    public boolean getAccepting() {
        return accepting;
    }
}
