package com.backend.dao;

import com.backend.entity.Dept;

public interface DeptDao {
	public Dept selectById(Integer deptId);
	public int insert(Dept entity);
	public int update(Dept entity);
	public int delete(Dept entity);
}
