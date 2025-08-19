package com.backend.dto;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.backend.entity.Enquete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerEnqueteDto implements Serializable {
	private Enquete enquete;
	private List<AnswerItem> answerList;

	public void addAnswerItem(AnswerItem answerItem) {
		answerList.add(answerItem);
	}
	
	public void sortAnswerListByQuestionNumber() {
		this.answerList.sort(Comparator.comparing(
		answer -> answer.getQuestionItem().getQuestion().getQuestionNumber()
		));
	}
	
	public void sortAnswerListByQuestionId() {
		this.answerList.sort(Comparator.comparing(
		answer -> answer.getQuestionItem().getQuestion().getQuestionId()
		));
	}
}
