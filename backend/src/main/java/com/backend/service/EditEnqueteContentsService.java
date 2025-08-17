package com.backend.service;

import com.backend.dto.EditEnqueteContentDto;
import com.backend.dto.TopAdminEnqueteDto;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.SystemException;
import com.backend.exception.UnauthorizedAccessException;

public interface EditEnqueteContentsService {
	public EditEnqueteContentDto getCopiedEnqueteContents(
			TopAdminEnqueteDto requestEnqDto, String adminEsqId)
			throws ItemNotFoundException, UnauthorizedAccessException;
	
	public EditEnqueteContentDto getSavedEnqueteContents(
			TopAdminEnqueteDto requestEnqDto, String adminEsqId)
			throws ItemNotFoundException, UnauthorizedAccessException;
	
	public void saveEnqueteContents(
			EditEnqueteContentDto editEnqueteContentDto, String adminEsqId)
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException;
}
