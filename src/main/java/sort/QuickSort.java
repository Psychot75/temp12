package sort;

/**
 * Concrete implementation of the Template Method pattern for Quick Sort.
 * notifyObservers() is inherited from Observable (via SortingAlgorithm).
 *
 * Quick Sort strategy:
 *   divide  → select a pivot, partition array around it
 *   conquer → recursively sort left and right sub-collections
 *   merge   → no-op (in-place partitioning handles it)
 */
public class QuickSort extends SortingAlgorithm {

    @Override
    public String getName() {
        return "Quick Sort";
    }

    /**
     * Primitive Operation — performSort
     * Pseudocode:
     *   if low < high:
     *     pivotIndex = divide(array, low, high)
     *     performSort(array, low, pivotIndex - 1)   // conquer left
     *     performSort(array, pivotIndex + 1, high)  // conquer right
     *     merge(array, low, pivotIndex, high)        // no-op for QuickSort
     */
    @Override
    protected void performSort(int[] array, int low, int high) {
        if (low < high) {
            int pivotIndex = divide(array, low, high);
            performSort(array, low, pivotIndex - 1);
            performSort(array, pivotIndex + 1, high);
            merge(array, low, pivotIndex, high);     // no-op
        }
    }

    /**
     * Primitive Operation — divide (Partition)
     * Pseudocode:
     *   pivot = array[high]
     *   i = low - 1
     *   for j from low to high - 1:
     *     if array[j] <= pivot:
     *       i++
     *       swap(array[i], array[j])
     *       highlight and notify
     *   swap(array[i+1], array[high])
     *   return i + 1
     */
    @Override
    protected int divide(int[] array, int low, int high) {
        int pivot = array[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            highlightedIndices = new int[]{j, high};
            notifyObservers();

            if (array[j] <= pivot) {
                i++;
                swap(array, i, j);
                highlightedIndices = new int[]{i, j};
                notifyObservers();
            }
        }

        swap(array, i + 1, high);
        highlightedIndices = new int[]{i + 1, high};
        notifyObservers();

        return i + 1;
    }

    /**
     * Primitive Operation — merge
     * No-op for Quick Sort: partitioning is done in-place during divide.
     */
    @Override
    protected void merge(int[] array, int low, int mid, int high) {
        // No-op: Quick Sort is in-place
    }

    // ─────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────

    private void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}