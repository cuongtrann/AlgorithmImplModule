package com.example.AlgorithmModuleImpl;

import com.example.AlgorithmModuleImpl.impl.InitSolution;
import com.example.AlgorithmModuleImpl.impl.TabuSearchImplement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

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
        System.out.println(LocalDateTime.now());
        int[][][] initialSolution = initSolution.initSolution();
        while (initialSolution == null){
            initialSolution = initSolution.initSolution();
        }
        int [][][] finalSolution = tabuSearchImplement.tabuSearch(initialSolution, 100, 50000);
        System.out.println(LocalDateTime.now());
        int[] subjectSlotStartInit = tabuSearchImplement.slotStartBySubjectMatrix(initialSolution);
        int [] subjectSlotStarBest = tabuSearchImplement.slotStartBySubjectMatrix(finalSolution);
        return tabuSearchImplement.calculateCostInit(initialSolution, subjectSlotStartInit) + "->" + tabuSearchImplement.calculateCostInit(finalSolution,subjectSlotStarBest);
    }
}
