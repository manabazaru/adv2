package com.backend.dao;

import java.util.List;

import com.backend.entity.Choice;

public interface ChoiceDao {
	public Choice selectById(Integer choiceId);
	public Choice selectByIdAndVersion(Integer choiceId, Integer version);
	public List<Choice> selectAllByQuestionId(Integer questionId);
	public int insert(Choice entity);
	public int update(Choice entity);
	public int delete(Choice entity);
	
}
