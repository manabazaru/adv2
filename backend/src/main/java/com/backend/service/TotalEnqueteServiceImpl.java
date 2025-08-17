package com.backend.service;

import org.springframework.stereotype.Service;

import com.backend.dao.ChoiceAnswerDao;
import com.backend.dao.ChoiceDao;
import com.backend.dao.DeptDao;
import com.backend.dao.EnqueteAdminUserDao;
import com.backend.dao.EnqueteAnswerDao;
import com.backend.dao.EnqueteDao;
import com.backend.dao.EnqueteDeptDao;
import com.backend.dao.EsqUserInfoDao;
import com.backend.dao.QuestionAnswerDao;
import com.backend.dao.QuestionDao;
import com.backend.dto.TopAdminEnqueteDto;
import com.backend.dto.TotalDto;
import com.backend.entity.AdminEnquete;
import com.backend.entity.EnqueteAdminUser;

@Service
public class TotalEnqueteServiceImpl implements TotalEnqueteService {
	private EsqUserInfoDao esqUserInfoDao;
	private DeptDao deptDao;
	private ChoiceDao choiceDao;
	private ChoiceAnswerDao choiceAnswerDao;
	private QuestionDao questionDao;
	private QuestionAnswerDao questionAnswerDao;
	private EnqueteDao enqueteDao;
	private EnqueteAnswerDao enqueteAnswerDao;
	private EnqueteDeptDao enqueteDeptDao;
	private EnqueteAdminUserDao enqueteAdminUserDao;
	
	public TotalEnqueteServiceImpl(
			EsqUserInfoDao esqUserInfoDao,
			DeptDao deptDao,
			ChoiceDao choiceDao,
			ChoiceAnswerDao choiceAnswerDao,
			QuestionDao questionDao,
			QuestionAnswerDao questionAnswerDao,
			EnqueteDao enqueteDao,
			EnqueteAnswerDao enqueteAnswerDao,
			EnqueteAdminUserDao enqueteAdminUserDao
			) {
		this.esqUserInfoDao = esqUserInfoDao;
		this.deptDao = deptDao;
		this.choiceDao = choiceDao;
		this.choiceAnswerDao = choiceAnswerDao;
		this.questionDao = questionDao;
		this.questionAnswerDao = questionAnswerDao;
		this.enqueteDao = enqueteDao;
		this.enqueteAnswerDao = enqueteAnswerDao;
		this.enqueteAdminUserDao = enqueteAdminUserDao;
	}
	
	@Override
	public TotalDto getEnqueteTotalInfo(TopAdminEnqueteDto requestEnqDto, String esqId){
		
		// リクエスト情報から enqueteId を取得
		AdminEnquete requestEnq = requestEnqDto.getAdminEnquete();
		Integer enqueteId = requestEnq.getEnqueteId();
		
		// 認証
		// 引数のesqIdに対するアンケート権限の確認
		EnqueteAdminUser adminUser = enqueteAdminUserDao.selectById(enqueteId, esqId);
		
		
		
		TotalDto totalDto = new TotalDto();
		
		
		return totalDto;
	}
	
	
	
	
	
	
	
	
	
}
