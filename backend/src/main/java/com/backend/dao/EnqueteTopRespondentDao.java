package com.backend.dao;

import java.util.List;

import com.backend.entity.RespondentEnquete;

public interface EnqueteTopRespondentDao {
	public List<RespondentEnquete> selectReceptionByEsqId(String esqId);
	public List<RespondentEnquete> selectResponseCompletedByEsqId(String esqId);
	public List<RespondentEnquete> selectResponseFinishedByEsqId(String esqId);
}
