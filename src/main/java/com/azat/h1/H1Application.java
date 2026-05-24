package com.azat.h1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class H1Application {

	public static void main(String[] args) {
		SpringApplication.run(H1Application.class, args);
	}

}
