package com.backend.dao;

import java.util.List;

import com.backend.entity.AdminEnquete;

public interface EnqueteTopAdminDao {
	public List<AdminEnquete> selectUnpublicByEsqId(String esqId);
	public List<AdminEnquete> selectPublicByEsqId(String esqId);
	public List<AdminEnquete> selectFinishedByEsqId(String esqId);
}
