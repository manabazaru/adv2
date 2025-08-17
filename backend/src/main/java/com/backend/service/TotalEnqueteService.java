package com.backend.service;

import com.backend.dto.TopAdminEnqueteDto;
import com.backend.dto.TotalDto;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.UnauthorizedAccessException;

public interface TotalEnqueteService {
	public TotalDto getEnqueteTotalInfo(
		TopAdminEnqueteDto requestEnqDto, String adminEsqId)
		throws ItemNotFoundException, UnauthorizedAccessException;
}
