package ui;

import sort.SortStep;

/**
 * Interface that VisualizationController depends on instead of the
 * concrete VisualizationPage.
 *
 * This breaks the circular dependency:
 *   VisualizationController → VisualizationDisplay ← VisualizationPage
 *
 * The controller package never imports the ui package directly.
 */
public interface VisualizationDisplay {

    /** Called before sorting begins — show initial unsorted state. */
    void reset(int[] array);

    /** Called on each replay step — update bars and highlight. */
    void updateDisplay(SortStep step);

    /** Called when replay is fully complete. */
    void notifySortComplete();
}