package com.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
import com.backend.dto.DeptAnswer;
import com.backend.dto.QuestionItem;
import com.backend.dto.TopAdminEnqueteDto;
import com.backend.dto.TotalDto;
import com.backend.dto.UserAnswerItem;
import com.backend.entity.AdminEnquete;
import com.backend.entity.Choice;
import com.backend.entity.ChoiceAnswer;
import com.backend.entity.Dept;
import com.backend.entity.Enquete;
import com.backend.entity.EnqueteAdminUser;
import com.backend.entity.EnqueteAnswer;
import com.backend.entity.EnqueteDept;
import com.backend.entity.EsqUserInfo;
import com.backend.entity.Question;
import com.backend.entity.QuestionAnswer;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.UnauthorizedAccessException;

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

	private void authorize(Integer enqueteId, String adminEsqId) 
		throws ItemNotFoundException, UnauthorizedAccessException{
		Enquete enquete = enqueteDao.selectById(enqueteId);
		
		EnqueteAdminUser adminUser = enqueteAdminUserDao.selectById(enqueteId, adminEsqId);
		if(adminUser == null) {
			throw new UnauthorizedAccessException("リクエストしたアンケートの管理者権限がありません。");
		}
		if(enquete == null) {
			throw new ItemNotFoundException("リクエストしたアンケートは存在しません。");
		}
	}
	
	@Override
	public TotalDto getEnqueteTotalInfo(TopAdminEnqueteDto requestEnqDto, String adminEsqId)
		 throws ItemNotFoundException, UnauthorizedAccessException{
		
		// リクエスト情報から enqueteId を取得
		AdminEnquete requestEnq = requestEnqDto.getAdminEnquete();
		Integer enqueteId = requestEnq.getEnqueteId();
		Enquete enquete = enqueteDao.selectById(enqueteId);
		
		
		// 認証 (失敗の場合は例外がスロー)
		authorize(enqueteId, adminEsqId);
		
		
		// 統計情報の設定 (アンケート情報, 質問情報, 回答情報)
		TotalDto totalDto = new TotalDto();
		// アンケート情報の格納
		totalDto.setEnquete(enquete);
		
		// 質問情報の格納
		// 必要な質問情報の取得
		List<Question> questionList = questionDao.selectAllByEnqueteId(enqueteId);
		questionList.sort(Comparator.comparing(question -> question.getQuestionNumber()));
		Integer questionSize = questionList.size();
		// questionIdとquestionNumberの対応HashMap
		Map<Integer, Integer> questionIdToNumber = new HashMap<>();
		// questionNumberごとのchoiceIdとchoiceNumberの対応HashMapリスト
		List<Map<Integer, Integer>> choiceIdToChoiceNumberList = new ArrayList<>();
		for(int i=0; i<questionSize+1; i++) {
			choiceIdToChoiceNumberList.add(new HashMap<>());
		}
		// 質問ごとに Dto 用クラス (QuestionItem) へ格納
		for(Question question: questionList) {
			Integer questionId = question.getQuestionId();
			Integer questionNumber = question.getQuestionNumber();
			List<Choice> choiceList = choiceDao.selectAllByQuestionId(questionId);
			
			QuestionItem questionItem = new QuestionItem(question, choiceList);
			questionItem.sortChoiceListByChoiceNumber();
			totalDto.addQuestionList(questionItem);
			
			questionIdToNumber.put(questionId, questionNumber);
			for(Choice choice: choiceList) {
				Integer choiceId = choice.getChoiceId();
				Integer choiceNumber = choice.getChoiceNumber();
				choiceIdToChoiceNumberList.get(questionNumber).put(choiceId, choiceNumber);
			}
		}
		
		// 回答情報の格納
		// 回答対象事業部の取得
		List<EnqueteDept> enqueteDeptList = enqueteDeptDao.selectAllByEnqueteId(enqueteId);
		List<Dept> tgtDeptList = new ArrayList<Dept>();
		for(EnqueteDept enqDept: enqueteDeptList) {
			tgtDeptList.add(deptDao.selectById(enqDept.getDeptId()));
		}
		// 回答者情報の取得
		List<EnqueteAnswer> enqAnswerList = enqueteAnswerDao.selectAllByEnqueteId(enqueteId);
		Stream<EnqueteAnswer> enqAnswerStream = enqAnswerList.stream();
		List<String> answeredEsqIdList = new ArrayList<String>();
		enqAnswerStream.filter(enqAnswer -> enqAnswer.getCompletedFlag()>0); // 一時保存中のアンケートは除外
		enqAnswerStream.forEach(enqAnswer -> answeredEsqIdList.add(enqAnswer.getEsqId()));
		enqAnswerList= enqAnswerStream.toList();
		
		// 事業部ごとの回答情報設定
		for(Dept dept: tgtDeptList) {
			DeptAnswer deptAnswer = new DeptAnswer();
			List<EsqUserInfo> esqUserList = esqUserInfoDao.selectAllByDeptId(dept.getDeptId());
			// 回答情報用リストの初期化
			List<List<UserAnswerItem>> answerList = new ArrayList<>(questionSize);
			for(int i=0; i<questionSize; i++) {answerList.add(new ArrayList<>());}
			
			// 回答状況の設定
			List<Boolean> hasResponseList = new ArrayList<Boolean>();
			for(int esqUserIdx = 0; esqUserIdx < esqUserList.size(); esqUserIdx++) {
				EsqUserInfo esqUser = esqUserList.get(esqUserIdx);
				String esqId = esqUser.getEsqId();
				int enqAnswerIdx = answeredEsqIdList.indexOf(esqId);
				// 回答している場合
				if(enqAnswerIdx > -1) {
					hasResponseList.add(true);
					// 回答が存在する場合は回答情報も格納
					EnqueteAnswer enqAnswer = enqAnswerList.get(enqAnswerIdx);
					List<QuestionAnswer> questionAnswerList = 
							questionAnswerDao.selectAllByEnqueteAnswerId(
									enqAnswer.getEnqueteAnswerId());
					for(QuestionAnswer qAnswer: questionAnswerList) {
						Integer questionId = qAnswer.getQuestionId();
						Integer questionNumber = questionIdToNumber.get(questionId);
						Object answer;
						List<ChoiceAnswer> choiceAnswerList = 
								choiceAnswerDao.selectAllByQuestionAnswerId(
										qAnswer.getQuestionAsnwerId());
						// 回答形式ごとに分岐
						switch(choiceAnswerList.size()) {
							// 単一回答
							case 1:
								ChoiceAnswer singleAnswer = choiceAnswerList.getFirst();
								Integer singleAnsId = singleAnswer.getChoiceId();
								answer = choiceIdToChoiceNumberList
									.get(questionNumber)
									.get(singleAnsId);
								break;
							// 自由記述
							case 0:
								answer = qAnswer.getAnswerText();
								break;
							// 複数回答
							default:
								List<Integer> multipleAnswer = new ArrayList<>();
								for(ChoiceAnswer choiceAnswer: choiceAnswerList) {
									Integer ansId = choiceAnswer.getChoiceAnswerId();
									Integer ansNum = choiceIdToChoiceNumberList
										.get(questionNumber)
										.get(ansId);
									multipleAnswer.add(ansNum);
								}
								multipleAnswer.sort(null);
								answer = multipleAnswer;
						}
						UserAnswerItem userAns = new UserAnswerItem(esqUser, answer);
						answerList.get(questionNumber-1).add(userAns);
					}
				// 回答していない場合
				}else {hasResponseList.add(false);}
			}
			
			deptAnswer.setDept(dept);
			deptAnswer.setEsqUserList(esqUserList);
			deptAnswer.setHasResponseList(hasResponseList);
			deptAnswer.setAnswerList(answerList);
			totalDto.addDeptAnswer(deptAnswer);
			
		}
		
		return totalDto;
	}
	
	
	
	
	
	
	
	
	
}
