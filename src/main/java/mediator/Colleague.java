package mediator;

public abstract class Colleague {

    protected final Mediator mediator;

    public Colleague(Mediator mediator) {
        this.mediator = mediator;
    }

    protected void send(String event, Object data) {
        mediator.notify(this, event, data);
    }

    protected void send(String event) {
        mediator.notify(this, event, null);
    }
}
