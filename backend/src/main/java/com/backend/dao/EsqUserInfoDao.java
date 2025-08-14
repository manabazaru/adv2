package com.backend.dao;

import java.util.List;

import com.backend.entity.EsqUserInfo;

public interface EsqUserInfoDao {
	public EsqUserInfo selectById(String esqId);
	public List<EsqUserInfo> selectAllByDeptId(Integer deptId);
	public int isnert(EsqUserInfo entity);
	public int update(EsqUserInfo entity);
	public int delete(EsqUserInfo entity);
}
