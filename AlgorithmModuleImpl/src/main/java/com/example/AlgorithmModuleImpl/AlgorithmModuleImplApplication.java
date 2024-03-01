package com.example.AlgorithmModuleImpl;

import com.sun.jdi.IntegerValue;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.*;

@SpringBootApplication
public class AlgorithmModuleImplApplication {

	public static void main(String[] args) {
		List<Integer> indice = new ArrayList<>();
		indice.add(1);
		indice.add(2);
		indice.add(3);
		indice.add(4);

		for (int i = indice.size() - 1; i >= 0; i--) {
			System.out.println(indice.get(i));
		}

		SpringApplication.run(AlgorithmModuleImplApplication.class, args);
	}

}
