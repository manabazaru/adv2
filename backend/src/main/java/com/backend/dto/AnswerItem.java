package com.backend.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerItem implements Serializable {
	private QuestionItem questionItem;
	
	private Object answer;
}
