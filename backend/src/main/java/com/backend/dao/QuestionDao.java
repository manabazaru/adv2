package com.backend.dao;

import java.util.List;

import com.backend.entity.Question;

public interface QuestionDao {
	public Question selectById(Integer questionId);
	public Question selectByIdAndVersion(Integer questionId, Integer version);
	public List<Question> selectAllByEnqueteId(Integer enqueteId);
	public int insert(Question entity);
	public int update(Question entity);
	public int delete(Question entity);
}
