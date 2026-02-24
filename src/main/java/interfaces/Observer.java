package interfaces;

public abstract class Observer {

    /**
     * Called upon an observable change
     */
    public abstract void update(Observable o);
}