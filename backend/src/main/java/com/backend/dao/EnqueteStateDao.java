package com.backend.dao;

import java.util.List;

import com.backend.entity.EnqueteState;

public interface EnqueteStateDao {
	public EnqueteState selectById(Integer enqueteStateid);
	public List<EnqueteState> selectAll();
	public int insert(EnqueteState entity);
	public int update(EnqueteState entity);
	public int delete(EnqueteState entity);
	
}
