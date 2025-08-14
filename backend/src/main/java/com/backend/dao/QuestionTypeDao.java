package com.backend.dao;

import com.backend.entity.QuestionType;

public interface QuestionTypeDao {
	public QuestionType selectById(Integer questionTypeId);
	public int insert(QuestionType entity);
	public int delete(QuestionType entity);
	public int update(QuestionType entity);
}
