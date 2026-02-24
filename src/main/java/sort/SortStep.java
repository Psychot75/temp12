package sort;

/**
 * Immutable snapshot of one sorting step.
 * Pulled by observers via SortingAlgorithm.getCurrentStep().
 */
public class SortStep {

    private final int[]   arrayState;
    private final int[]   highlightedIndices;
    private final boolean sortComplete;        // true on the final notification

    public SortStep(int[] arrayState, int[] highlightedIndices, boolean sortComplete) {
        this.arrayState         = arrayState;
        this.highlightedIndices = highlightedIndices;
        this.sortComplete       = sortComplete;
    }

    public int[]   getArrayState()        { return arrayState; }
    public int[]   getHighlightedIndices(){ return highlightedIndices; }
    public boolean isSortComplete()       { return sortComplete; }
}