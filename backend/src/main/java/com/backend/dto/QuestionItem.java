package com.backend.dto;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.backend.entity.Choice;
import com.backend.entity.Question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionItem implements Serializable{
	private Question question;
	private List<Choice> choiceList;
	
	public void sortChoiceListByChoiceNumber() {
		this.choiceList.sort(Comparator.comparing(
		choice -> choice.getChoiceNumber()
		));
	}
	
	public void sortChoiceListByChoiceId() {
		this.choiceList.sort(Comparator.comparing(
		choice -> choice.getChoiceId()
		));
	}
}
