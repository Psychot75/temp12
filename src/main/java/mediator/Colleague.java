package mediator;

/**
 * Abstract Colleague.
 * All UI pages extend this class. They hold a reference to the Mediator
 * and never communicate with other colleagues directly.
 */
public abstract class Colleague {

    protected final Mediator mediator;

    public Colleague(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Convenience — send an event to the mediator.
     */
    protected void send(String event, Object data) {
        mediator.notify(this, event, data);
    }

    protected void send(String event) {
        mediator.notify(this, event, null);
    }
}