package ui;

import sort.SortStep;

public interface VisualizationDisplay {
    void reset(int[] array);
    void updateDisplay(SortStep step);
    void notifySortComplete();
}
