package com.backend.dao;

import com.backend.entity.Enquete;

public interface EnqueteDao {
	public Enquete selectById(Integer enqueteId);
	public Enquete selectByIdAndVersion(Integer enqueteId, Integer version);
	public int insert(Enquete entity);
	public int update(Enquete entity);
	public int delete(Enquete entity);
}
