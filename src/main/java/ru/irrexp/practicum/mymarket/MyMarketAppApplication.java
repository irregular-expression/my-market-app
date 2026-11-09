package ru.irrexp.practicum.mymarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MyMarketAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyMarketAppApplication.class, args);
	}

}
