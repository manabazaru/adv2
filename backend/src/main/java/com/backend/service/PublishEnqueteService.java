package com.backend.service;

import com.backend.dto.EnqueteDto;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.SystemException;
import com.backend.exception.UnauthorizedAccessException;

public interface PublishEnqueteService {
	public void publishEnquete(
			EnqueteDto enqueteDto, String esqId) 
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException;
}
