package space.nebulark.junisockets.errors;

public class SuffixDoesNotExist extends Exception {
   
    /**
     *
     */
    private static final long serialVersionUID = -7487446103951806431L;

    public SuffixDoesNotExist() {
        super("suffix does not exist");
    }

}
