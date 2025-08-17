package com.backend.dao;

import java.util.List;

import com.backend.entity.EnqueteAdminUser;

public interface EnqueteAdminUserDao {
	public EnqueteAdminUser selectById(Integer enqueteId, String esqId);
	public List<EnqueteAdminUser> selectAllByEnqueteId(Integer enqueteId);
	public int insert(EnqueteAdminUser entity);
	public int update(EnqueteAdminUser entity);
	public int delete(EnqueteAdminUser entity);
}
