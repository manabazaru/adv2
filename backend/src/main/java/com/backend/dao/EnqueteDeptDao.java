package com.backend.dao;

import java.util.List;

import com.backend.entity.EnqueteDept;

public interface EnqueteDeptDao {
	public EnqueteDept selectById(Integer enqueteId, Integer deptId);
	public List<EnqueteDept> selectAllByEnqueteId(Integer enqueteId);
	public int insert(EnqueteDept entity);
	public int update(EnqueteDept entity);
	public int delete(EnqueteDept entity);
	
}
