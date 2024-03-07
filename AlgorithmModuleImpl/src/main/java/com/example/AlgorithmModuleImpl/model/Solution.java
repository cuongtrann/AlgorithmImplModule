package com.example.AlgorithmModuleImpl.model;

public class Solution {
    private int[][][] solution;
    private double fitness;

    public Solution(int[][][] solution, double fitness) {
        this.solution = solution;
        this.fitness = fitness;
    }

    public int[][][] getSolution() {
        return solution;
    }

    public double getFitness() {
        return fitness;
    }
}
