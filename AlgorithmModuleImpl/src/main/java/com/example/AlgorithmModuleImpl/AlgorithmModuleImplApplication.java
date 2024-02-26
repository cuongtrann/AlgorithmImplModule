package com.example.AlgorithmModuleImpl;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class AlgorithmModuleImplApplication {

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		for (int i:
			 list) {
			System.out.println(i);
		}
		List<Integer> listA = new ArrayList<>();
		listA.add(2);
		listA.add(4);

		list.removeAll(listA);
		System.out.println("After");
		for (int i:
				list) {
			System.out.println(i);
		}
		SpringApplication.run(AlgorithmModuleImplApplication.class, args);
	}

}
