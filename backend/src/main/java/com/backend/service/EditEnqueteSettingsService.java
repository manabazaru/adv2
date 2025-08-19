package com.backend.service;

import com.backend.dto.EditEnqueteSettingsDto;
import com.backend.dto.EnqueteDto;
import com.backend.dto.TopAdminEnqueteDto;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.SystemException;
import com.backend.exception.UnauthorizedAccessException;

public interface EditEnqueteSettingsService {
	public EditEnqueteSettingsDto getSavedEnqueteSettings(
			EnqueteDto enqueteDto,String esqId)
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException; 

	public EditEnqueteSettingsDto getSavedEnqueteSettings(
			TopAdminEnqueteDto enqueteDto,String esqId)
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException;
	
	public void notifyTeams(EnqueteDto enqueteDto, String esqId)
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException;
	
	public void saveEnqueteSettings(
			EditEnqueteSettingsDto editEnqueteSettingsDto, String esqId)
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException;
}
