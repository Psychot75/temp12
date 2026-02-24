package sort;

import interfaces.Observable;

/**
 * Abstract base class implementing the Template Method pattern.
 * Extends Observable so concrete algorithms can notify observers (pull model).
 *
 * Template Method: sort()
 *   1. initialize()            ← hook (optional override)
 *   2. performSort()           ← abstract primitive operation
 *   3. notifyObservers()       ← inherited from Observable
 *
 * Observers retrieve state by calling getCurrentStep() on this object.
 */
public abstract class SortingAlgorithm extends Observable {

    // Current array state — exposed for observer pull
    protected int[] array;

    // Indices currently being compared/moved (for bar highlighting)
    protected int[] highlightedIndices = new int[0];

    // Flag set when sorting is fully complete
    private boolean sortComplete = false;

    // ─────────────────────────────────────────────
    //  TEMPLATE METHOD  (final — cannot be overridden)
    // ─────────────────────────────────────────────

    /**
     * Template method — defines the overall sorting process.
     *
     * Pseudocode:
     *   array = copy of data
     *   initialize()
     *   notifyObservers()                          ← initial state
     *   performSort(array, 0, array.length - 1)
     *   highlightedIndices = []
     *   sortComplete = true
     *   notifyObservers()                          ← final sorted state
     */
    public final void sort(int[] data) {
        this.array = data.clone();
        this.sortComplete = false;
        initialize();
        notifyObservers();                                   // show initial state
        performSort(array, 0, array.length - 1);
        highlightedIndices = new int[0];
        sortComplete = true;
        notifyObservers();                                   // show final state
    }

    // ─────────────────────────────────────────────
    //  PRIMITIVE OPERATIONS  (must be overridden)
    // ─────────────────────────────────────────────

    /**
     * Core recursive sort — implemented differently per algorithm.
     *
     * Pseudocode:
     *   divide the collection [low..high]
     *   conquer each sub-collection recursively
     *   merge the sub-collections back together
     */
    protected abstract void performSort(int[] array, int low, int high);

    /**
     * Splits the segment into two sub-collections.
     * Returns the partition/mid index.
     *
     * Pseudocode:
     *   QuickSort → select pivot, partition around it, return pivot index
     *   MergeSort → return (low + high) / 2
     */
    protected abstract int divide(int[] array, int low, int high);

    /**
     * Merges/combines two sorted sub-collections.
     *
     * Pseudocode:
     *   QuickSort → no-op (in-place partitioning)
     *   MergeSort → merge left[low..mid] and right[mid+1..high]
     */
    protected abstract void merge(int[] array, int low, int mid, int high);

    // ─────────────────────────────────────────────
    //  HOOK OPERATION  (optional override)
    // ─────────────────────────────────────────────

    /**
     * Called before sorting begins. Override to add setup logic.
     */
    protected void initialize() {}

    // ─────────────────────────────────────────────
    //  OBSERVER PULL — state exposed to observers
    // ─────────────────────────────────────────────

    /**
     * Returns a snapshot of the current sorting step.
     * Observers call this inside their update(Observable o) method.
     */
    public SortStep getCurrentStep() {
        return new SortStep(array.clone(), highlightedIndices.clone(), sortComplete);
    }

    public abstract String getName();
}