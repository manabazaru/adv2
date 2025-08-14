package com.backend.dao;

import java.util.List;

import com.backend.entity.QuestionAnswer;

public interface QuestionAnswerDao {
	public QuestionAnswer selectById(Integer questionAnswerId);
	public List<QuestionAnswer> selectAllByEnqueteAnswerId(Integer enqueteAnswerId);
	public int insert(QuestionAnswer entity);
	public int update(QuestionAnswer entity);
	public int delete(QuestionAnswer entity);
}
