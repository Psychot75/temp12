package mediator;

/**
 * Mediator interface.
 * Colleagues communicate exclusively through this contract —
 * they never reference each other directly.
 */
public interface Mediator {

    /**
     * A colleague signals an event to the mediator.
     *
     * @param sender the colleague sending the signal
     * @param event  identifier string (e.g. "START_SORT", "BACK", "SORT_COMPLETE")
     * @param data   optional payload (e.g. SortingParameters, null)
     */
    void notify(Colleague sender, String event, Object data);
}