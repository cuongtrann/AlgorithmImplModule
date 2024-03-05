package com.example.AlgorithmModuleImpl;

import com.example.AlgorithmModuleImpl.impl.InitSolution;
import com.example.AlgorithmModuleImpl.impl.TabuSearchImplement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlgorithmController {
    private final InitSolution initSolution;
    private final TabuSearchImplement tabuSearchImplement;
    public AlgorithmController(InitSolution initSolution, TabuSearchImplement tabuSearchImplement) {
        this.initSolution = initSolution;
        this.tabuSearchImplement = tabuSearchImplement;
    }

    @GetMapping("/")
    public String index() {
        int[][][] initialSolution = initSolution.initSolution();
        while (initialSolution == null){
            initialSolution = initSolution.initSolution();
        }
        int [][][] finalSolution = tabuSearchImplement.tabuSearch(initialSolution, 50, 50);
        return tabuSearchImplement.calculateCost(initialSolution) + "->" + tabuSearchImplement.calculateCost(finalSolution);
    }
}
