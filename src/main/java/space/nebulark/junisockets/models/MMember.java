package space.nebulark.junisockets.models;

/**
 * MMember
 */
public class MMember {
    private int[] ports;

    /**
     * Constructor MMember
     * @param ports ports
     */
    public MMember(int[] ports) {
        this.ports = ports;
    }
    

    /** 
     * Returns ports
     * @return int[]
     */
    public int[] getPorts() {
        return ports;
    }
}
