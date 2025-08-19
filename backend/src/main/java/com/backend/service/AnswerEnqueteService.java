package com.backend.service;

import com.backend.dto.AnswerEnqueteDto;
import com.backend.dto.TopRespondentEnqueteDto;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.SystemException;
import com.backend.exception.UnauthorizedAccessException;

public interface AnswerEnqueteService {
	
	public AnswerEnqueteDto getEnqueteAnswer(
			TopRespondentEnqueteDto requestEnqDto, String esqId, boolean isReadOnly) 
			throws ItemNotFoundException, UnauthorizedAccessException;
	
	public void saveEnqueteAnswer(
			AnswerEnqueteDto answerEnqueteDto, String esqId) 
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException;

	public void completeEnqueteAnswer(
			AnswerEnqueteDto answerEnqueteDto, String esqId) 
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException;
}
