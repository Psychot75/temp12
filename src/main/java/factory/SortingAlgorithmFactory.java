package factory;

import sort.SortingAlgorithm;
import sort.QuickSort;
import sort.MergeSort;

public class SortingAlgorithmFactory {

    public enum AlgorithmType {
        QUICK_SORT,
        MERGE_SORT
    }

    public static SortingAlgorithm create(AlgorithmType type) {
        return switch (type) {
            case QUICK_SORT -> new QuickSort();
            case MERGE_SORT -> new MergeSort();
        };
    }
}
