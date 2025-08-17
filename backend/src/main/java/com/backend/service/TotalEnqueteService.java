package com.backend.service;

import com.backend.dto.TopAdminEnqueteDto;
import com.backend.dto.TotalDto;

public interface TotalEnqueteService {
	public TotalDto getEnqueteTotalInfo(TopAdminEnqueteDto adminEnqueteDto, String esqId);
}
