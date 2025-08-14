package com.backend.dao;

import com.backend.entity.EsqUser;

public interface EsqUserDao {
	public EsqUser selectById(String esqId);
	public int isnert(EsqUser entity);
	public int update(EsqUser entity);
	public int delete(EsqUser entity);
}
