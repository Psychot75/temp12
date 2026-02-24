package model;

import factory.SortingAlgorithmFactory.AlgorithmType;

public class SortingParameters {

    private int[] collection;
    private AlgorithmType algorithmType;
    private SimulationSpeed speed;

    public SortingParameters(int[] collection, AlgorithmType algorithmType, SimulationSpeed speed) {
        this.collection = collection;
        this.algorithmType = algorithmType;
        this.speed = speed;
    }

    public int[] getCollection() { return collection; }
    public void setCollection(int[] collection) { this.collection = collection; }
    public AlgorithmType getAlgorithmType() { return algorithmType; }
    public void setAlgorithmType(AlgorithmType algorithmType) { this.algorithmType = algorithmType; }
    public SimulationSpeed getSpeed() { return speed; }
    public void setSpeed(SimulationSpeed speed) { this.speed = speed; }
}
