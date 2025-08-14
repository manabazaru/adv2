package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {
	private Integer questionId;
	private Integer enqueteId;
	private Integer questionNumber;
	private Integer questionTypeId;
	private Integer requireFlag;
	private String questionText;
	private String questionSubtext;
	private Integer version;
}
