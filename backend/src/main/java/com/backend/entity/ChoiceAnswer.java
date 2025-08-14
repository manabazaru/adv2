package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChoiceAnswer {
	
	private Integer choiceAnswerId;
	private Integer questionAnswerId;
	private Integer choiceId;
	
}
