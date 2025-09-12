package com.backend.Trigger;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.service.DailyJobService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DailyJobTriggers {
	private final DailyJobService job;
	
	@EventListener(ApplicationReadyEvent.class)
	public void onStartup() {
		job.executeDailyJob();
	}
	
	@Scheduled(cron = "0 7 15 * * *", zone="Asia/Tokyo")
	public void onEveryDay() {
		job.executeDailyJob();
	}
}
