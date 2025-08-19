package com.backend.service;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.backend.dao.EnqueteAdminUserDao;
import com.backend.dao.EnqueteDao;
import com.backend.dto.EnqueteDto;
import com.backend.entity.Enquete;
import com.backend.entity.EnqueteAdminUser;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.SystemException;
import com.backend.exception.UnauthorizedAccessException;

@Service
public class PublishEnqueteServiceImpl implements PublishEnqueteService{
	private EnqueteDao enqueteDao;
	private EnqueteAdminUserDao enqueteAdminUserDao;
	
	public PublishEnqueteServiceImpl(
			EnqueteDao enqueteDao,
			EnqueteAdminUserDao enqueteAdminUserDao
			) {
		this.enqueteDao = enqueteDao;
		this.enqueteAdminUserDao = enqueteAdminUserDao;
	}

	
	public void publishEnquete(EnqueteDto enqueteDto, String esqId) 
		throws ItemNotFoundException, UnauthorizedAccessException, SystemException{
		
		Enquete enquete = enqueteDto.getEnquete();
		Integer enqueteId = enquete.getEnqueteId();
		
		// 認証
		EnqueteAdminUser adminUser = enqueteAdminUserDao.selectById(enqueteId, esqId);
		if(adminUser == null || adminUser.getDeleteFlag().equals(Integer.valueOf(1))) {
			throw new UnauthorizedAccessException("リクエストしたアンケートの管理者権限がありません。");
		}
		if(enqueteDao.selectById(enqueteId) == null) {
			throw new ItemNotFoundException("リクエストしたアンケートは存在しません。");
		}
		
		// 公開設定の変更
		Integer stateId = Integer.valueOf(3);
		enquete.setEnqueteStateId(stateId);
		
		// 公開日の記録
		LocalDate localDate = LocalDate.now();
		Date startDate = Date.valueOf(localDate);
		enquete.setStartDate(startDate);
		
		enqueteDao.update(enquete);
	}
}
