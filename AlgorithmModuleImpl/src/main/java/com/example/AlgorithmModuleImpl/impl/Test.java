package com.example.AlgorithmModuleImpl.impl;

import com.example.AlgorithmModuleImpl.data.ImplementData;
import com.example.AlgorithmModuleImpl.model.Solution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test {
    public void payoffPi(){
        for (int i = 1; i <= 7 ; i++) {
            for (int j = 1; j <= 42 ; j++) {
                // i: Số môn sinh viên đó thi
                // j: Số slot thi
                for (int k = 1; k <= i; k++) {

                }
            }
        }
    }

    // 5 môn
    public static double payOff5Subject(){
        double maxPayoff = 0;
        double currentPayoff = 0;
        int[] bestSolution = new int[0];
        double meanDistance = (double) ImplementData.TOTAL_EXAM_SLOTS / 5;
        for (int i = 1; i <= ImplementData.TOTAL_EXAM_SLOTS - 4; i++) {
            for (int j = i+1; j <= ImplementData.TOTAL_EXAM_SLOTS - 3; j++) {
                for (int k = j+1; k <= ImplementData.TOTAL_EXAM_SLOTS - 2; k++) {
                    for (int l = k+1; l <= ImplementData.TOTAL_EXAM_SLOTS - 1; l++) {
                        for (int m = l+1; m <= ImplementData.TOTAL_EXAM_SLOTS; m++) {
                            currentPayoff += Math.abs(m-l-meanDistance) + Math.abs(l-k-meanDistance) + Math.abs(k-j-meanDistance) + Math.abs(j-i-meanDistance);
                            if(currentPayoff >= maxPayoff){
                                maxPayoff = currentPayoff;
                                bestSolution = new int[]{m,l,k,j,i};
                            }
                            currentPayoff = 0;
                        }
                    }
                }
            }
        }
        return maxPayoff;
    }

    public static double payOff6Subject(){
        double maxPayoff = 0;
        double currentPayoff = 0;
        int[] bestSolution = new int[0];
        double meanDistance = (double) ImplementData.TOTAL_EXAM_SLOTS / 5;
        for (int i = 1; i <= ImplementData.TOTAL_EXAM_SLOTS - 5; i++) {
            currentPayoff = 0;
            for (int j = i+1; j <= ImplementData.TOTAL_EXAM_SLOTS - 4; j++) {
                for (int k = j+1; k <= ImplementData.TOTAL_EXAM_SLOTS - 3; k++) {
                    for (int l = k+1; l <= ImplementData.TOTAL_EXAM_SLOTS - 2; l++) {
                        for (int m = l+1; m <= ImplementData.TOTAL_EXAM_SLOTS - 1; m++) {
                            for (int n = m+1; n <= ImplementData.TOTAL_EXAM_SLOTS ; n++) {
                                currentPayoff += Math.pow(n-m-meanDistance, 2) + Math.pow(m-l-meanDistance, 2) + Math.pow(l-k-meanDistance, 2) + Math.pow(k-j-meanDistance, 2) + Math.pow(j-i-meanDistance, 2);
                                if(currentPayoff > maxPayoff){
                                    maxPayoff = currentPayoff;
                                    bestSolution = new int[]{n,m,l,k,j,i};
                                }
                            }
                        }
                    }
                }
            }
        }
        return maxPayoff;
    }
}
