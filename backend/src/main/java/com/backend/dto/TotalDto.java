package com.backend.dto;

import java.io.Serializable;
import java.util.List;

import com.backend.entity.Enquete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalDto implements Serializable {
	private Enquete enquete;
	private List<DeptAnswer> deptAnswerList;
	private List<QuestionItem> questionList;
	
	public void addDeptAnswer(DeptAnswer deptAnswer) {
		deptAnswerList.add(deptAnswer);
	}
	
	public void addQuestionList(QuestionItem questionItem) {
		questionList.add(questionItem);
	}
	
}
