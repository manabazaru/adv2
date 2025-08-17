package com.backend.dto;

import java.util.Comparator;
import java.util.List;

import com.backend.entity.Enquete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditEnqueteContentDto {
	private Enquete enquete;
	private List<QuestionItem> questionList;
	
	public void addQuestionItem(QuestionItem questionItem) {
		questionList.add(questionItem);
	}
	
	public void sortQuestionListByQuestionNumber() {
		this.questionList.sort(Comparator.comparing(
		question -> question.getQuestion().getQuestionId()
		));
	}
	
	public void sortQuestionListByQuestionId() {
		this.questionList.sort(Comparator.comparing(
		question -> question.getQuestion().getQuestionNumber()));
	}
}
