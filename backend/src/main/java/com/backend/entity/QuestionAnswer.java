package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAnswer {
	private Integer questionAsnwerId;
	private Integer enqueteAnswerId;
	private Integer questionId;
	private String answerText;
}
