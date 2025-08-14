package com.backend.dao;

import java.util.List;

import com.backend.entity.EnqueteAnswer;

public interface EnqueteAnswerDao {
	public EnqueteAnswer selectById(Integer enqueteAnswerId);
	public List<EnqueteAnswer> selectAllByEnqueteId(Integer enqueteId);
	public int insert(EnqueteAnswer entity);
	public int update(EnqueteAnswer entity);
	public int delete(EnqueteAnswer entity);
}
