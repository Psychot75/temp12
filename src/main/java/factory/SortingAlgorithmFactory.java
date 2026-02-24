package factory;

import sort.SortingAlgorithm;
import sort.QuickSort;
import sort.MergeSort;

/**
 * Factory class for creating SortingAlgorithm instances.
 *
 * Factory Pattern: decouples algorithm instantiation from the client code.
 * To add a new algorithm, only this factory needs to change.
 */
public class SortingAlgorithmFactory {

    public enum AlgorithmType {
        QUICK_SORT,
        MERGE_SORT
    }

    /**
     * Creates and returns the requested sorting algorithm.
     *
     * @param type the algorithm type to instantiate
     * @return a new SortingAlgorithm instance
     * @throws IllegalArgumentException if the type is not supported
     */
    public static SortingAlgorithm create(AlgorithmType type) {
        return switch (type) {
            case QUICK_SORT -> new QuickSort();
            case MERGE_SORT -> new MergeSort();
            default -> throw new IllegalArgumentException("Unknown algorithm: " + type);
        };
    }

    /**
     * Convenience overload — creates algorithm from its display name string.
     */
    public static SortingAlgorithm create(String name) {
        return switch (name.toLowerCase().replace(" ", "")) {
            case "quicksort" -> new QuickSort();
            case "mergesort" -> new MergeSort();
            default -> throw new IllegalArgumentException("Unknown algorithm: " + name);
        };
    }
}