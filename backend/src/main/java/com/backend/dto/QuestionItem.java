package com.backend.dto;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.backend.entity.Choice;
import com.backend.entity.Question;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionItem implements Serializable{
	private Question question;
	private List<Choice> choiceList;
	
	public QuestionItem(Question question, List<Choice> choiceList) {
		this.question = question;
		choiceList.sort(Comparator.comparing(choice -> choice.getChoiceNumber()));
		this.choiceList = choiceList;
	}
	
	public List<Choice> getChoiceList(){
		choiceList.sort(Comparator.comparing(choice -> choice.getChoiceId()));
		return choiceList;
		
	}
}
