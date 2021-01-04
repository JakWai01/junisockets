package space.nebulark.junisockets.models;

public class MMember {
    private int[] ports;

    public MMember(int[] ports) {
        this.ports = ports;
    }

    
    /** 
     * @return int[]
     */
    public int[] getPorts() {
        return ports;
    }
}
