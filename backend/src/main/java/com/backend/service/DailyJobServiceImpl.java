package com.backend.service;

import org.springframework.stereotype.Service;

@Service
public class DailyJobServiceImpl implements DailyJobService{
	
	public void executeDailyJob() {
		System.out.println("hello, this is in DailyJobService.");
		System.out.println("Now method is executed.");
	}
}
