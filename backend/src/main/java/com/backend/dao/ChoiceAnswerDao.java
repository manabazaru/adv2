package com.backend.dao;

import java.util.List;

import com.backend.entity.ChoiceAnswer;

public interface ChoiceAnswerDao {
	
	public ChoiceAnswer selectById(Integer choiceAnswerId);
	public List<ChoiceAnswer> selectAllByQuestionAnswerId(Integer questionAnswerId);
	public int insert(ChoiceAnswer entity);
	public int update(ChoiceAnswer entity);
	public int delete(ChoiceAnswer entity);
}
