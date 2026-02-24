package interfaces;

import java.util.ArrayList;
import java.util.List;

public class Observable {

    /**
     * The list of observers of the observable
     */
    private final List<Observer> observers = new ArrayList<>();

    /**
     * Attaches the node to an observer
     * @param o the observer to attach to
     */
    public void attach(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    /**
     * Detaches the node from an observer
     * @param o the observer to detach from
     */
    public void detach(Observer o) {
        observers.remove(o);
    }

    /**
     * Notify the observers of a change
     */
    protected void notifyObservers() {
        for (Observer o : observers) {
            o.update(this);
        }
    }
}