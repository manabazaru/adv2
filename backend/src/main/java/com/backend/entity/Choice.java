package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Choice {
	
	private Integer choiceId;
	private Integer questionId;
	private Integer choiceNumber;
	private String choiceText;
	private Integer version;
}
