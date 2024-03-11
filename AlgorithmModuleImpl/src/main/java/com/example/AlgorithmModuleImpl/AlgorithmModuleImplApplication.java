package com.example.AlgorithmModuleImpl;

import com.example.AlgorithmModuleImpl.impl.Test;
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
		int payOffMax5 = (int) Test.payOff5Subject();
		double payOffMax6 = Test.payOff6Subject();
		System.out.println(payOffMax5);
		SpringApplication.run(AlgorithmModuleImplApplication.class, args);
	}

	private static String convertArrayToString(int[] array) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int num : array) {
			stringBuilder.append(num).append(",");
		}
		return stringBuilder.toString();
	}

}
