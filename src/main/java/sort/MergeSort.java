package sort;

/**
 * Concrete implementation of the Template Method pattern for Merge Sort.
 * notifyObservers() is inherited from Observable (via SortingAlgorithm).
 *
 * Merge Sort strategy:
 *   divide  → split in two equal halves at mid = (low + high) / 2
 *   conquer → recursively sort each half
 *   merge   → merge both sorted halves into one sorted collection
 */
public class MergeSort extends SortingAlgorithm {

    @Override
    public String getName() {
        return "Merge Sort";
    }

    /**
     * Primitive Operation — performSort
     * Pseudocode:
     *   if low < high:
     *     mid = divide(array, low, high)
     *     performSort(array, low, mid)       // conquer left
     *     performSort(array, mid + 1, high)  // conquer right
     *     merge(array, low, mid, high)        // combine sorted halves
     */
    @Override
    protected void performSort(int[] array, int low, int high) {
        if (low < high) {
            int mid = divide(array, low, high);
            performSort(array, low, mid);
            performSort(array, mid + 1, high);
            merge(array, low, mid, high);
        }
    }

    /**
     * Primitive Operation — divide
     * Pseudocode:
     *   return (low + high) / 2
     */
    @Override
    protected int divide(int[] array, int low, int high) {
        return (low + high) / 2;
    }

    /**
     * Primitive Operation — merge
     * Pseudocode:
     *   copy left[low..mid] and right[mid+1..high]
     *   i = 0, j = 0, k = low
     *   while i < left.length and j < right.length:
     *     if left[i] <= right[j]: array[k++] = left[i++]
     *     else:                   array[k++] = right[j++]
     *     highlight k and notify
     *   copy remaining elements
     *   notify
     */
    @Override
    protected void merge(int[] array, int low, int mid, int high) {
        int leftSize  = mid - low + 1;
        int rightSize = high - mid;

        int[] left  = new int[leftSize];
        int[] right = new int[rightSize];

        System.arraycopy(array, low,       left,  0, leftSize);
        System.arraycopy(array, mid + 1,   right, 0, rightSize);

        int i = 0, j = 0, k = low;

        while (i < leftSize && j < rightSize) {
            highlightedIndices = new int[]{low + i, mid + 1 + j};
            notifyObservers();

            if (left[i] <= right[j]) {
                array[k++] = left[i++];
            } else {
                array[k++] = right[j++];
            }

            highlightedIndices = new int[]{k - 1};
            notifyObservers();
        }

        while (i < leftSize) {
            array[k] = left[i++];
            highlightedIndices = new int[]{k};
            notifyObservers();
            k++;
        }

        while (j < rightSize) {
            array[k] = right[j++];
            highlightedIndices = new int[]{k};
            notifyObservers();
            k++;
        }
    }
}