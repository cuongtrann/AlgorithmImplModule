package com.example.AlgorithmModuleImpl;

import com.example.AlgorithmModuleImpl.data.ImplementData;
import com.example.AlgorithmModuleImpl.impl.TabuSearch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlgorithmController {
    private final TabuSearch tabuSearch;

    public AlgorithmController(TabuSearch tabuSearch) {
        this.tabuSearch = tabuSearch;
    }

    @GetMapping("/")
    public String index() {
        tabuSearch.initSolution();
        return "Greetings from Spring Boot!";
    }
}
