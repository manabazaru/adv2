package com.backend.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnqueteAnswer {
	private Integer enqueteAnswerId;
	private Integer enqueteId;
	private String esqId;
	private Date answerDate;
	private Integer completedFlag;
}
