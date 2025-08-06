package com.mynthon.task.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@EnableFeignClients
@Modulithic(sharedModules = "common", additionalPackages = {"com.mynthon.task.manager.*.internal.model","com.mynthon.task.manager.*.internal.mapper"})
public class TaskManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskManagerApplication.class, args);
	}

}
