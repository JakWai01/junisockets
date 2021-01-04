package space.nebulark.junisockets.errors;

/**
 * Suffix does not exist
 */
public class SuffixDoesNotExist extends Exception {
   
    /**
     *
     */
    private static final long serialVersionUID = -7487446103951806431L;

    /**
     * Constructor SuffixDoesNotExist
     */
    public SuffixDoesNotExist() {
        super("suffix does not exist");
    }

}
