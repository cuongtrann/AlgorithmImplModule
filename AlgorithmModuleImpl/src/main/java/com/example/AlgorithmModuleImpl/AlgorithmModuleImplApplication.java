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
		int[] array1 = {1, 2, 3, 4, 5};
		int[] array2 = {1, 2, 3, 4, 5};
		int[] array3 = {6, 7, 8, 9, 10};

		List<int[]> set = new ArrayList<>();
		set.add(array1);

		System.out.println("Contains array1: " + containsArray(set, array1)); // true
		System.out.println("Contains array2: " + containsArray(set, array2)); // true (vì giá trị giống array1)
		System.out.println("Contains array3: " + containsArray(set, array3)); // false
		SpringApplication.run(AlgorithmModuleImplApplication.class, args);
	}

	private static boolean containsArray(List<int[]> list, int[] array) {
		return list.stream().anyMatch(arr -> Arrays.equals(arr, array));
	}

}
