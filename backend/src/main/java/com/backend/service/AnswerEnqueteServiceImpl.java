package com.backend.service;

import java.util.ArrayList;
import java.util.List;

import com.backend.dao.ChoiceAnswerDao;
import com.backend.dao.ChoiceDao;
import com.backend.dao.EnqueteAnswerDao;
import com.backend.dao.EnqueteDao;
import com.backend.dao.EnqueteDeptDao;
import com.backend.dao.EsqUserInfoDao;
import com.backend.dao.QuestionAnswerDao;
import com.backend.dao.QuestionDao;
import com.backend.dto.AnswerEnqueteDto;
import com.backend.dto.AnswerItem;
import com.backend.dto.QuestionItem;
import com.backend.dto.TopRespondentEnqueteDto;
import com.backend.entity.Choice;
import com.backend.entity.ChoiceAnswer;
import com.backend.entity.Enquete;
import com.backend.entity.EnqueteAnswer;
import com.backend.entity.EnqueteDept;
import com.backend.entity.EsqUserInfo;
import com.backend.entity.Question;
import com.backend.entity.QuestionAnswer;
import com.backend.entity.RespondentEnquete;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.SystemException;
import com.backend.exception.UnauthorizedAccessException;

public class AnswerEnqueteServiceImpl implements AnswerEnqueteService{
	
	private EsqUserInfoDao esqUserInfoDao;
	private ChoiceDao choiceDao;
	private ChoiceAnswerDao choiceAnswerDao;
	private QuestionDao questionDao;
	private QuestionAnswerDao questionAnswerDao;
	private EnqueteDao enqueteDao;
	private EnqueteAnswerDao enqueteAnswerDao;
	private EnqueteDeptDao enqueteDeptDao;
	
	public AnswerEnqueteServiceImpl(
			EsqUserInfoDao esqUserInfoDao,
			ChoiceDao choiceDao,
			ChoiceAnswerDao choiceAnswerDao,
			QuestionDao questionDao,
			QuestionAnswerDao questionAnswerDao,
			EnqueteDao enqueteDao,
			EnqueteAnswerDao enqueteAnswerDao
			) {
		this.esqUserInfoDao = esqUserInfoDao;
		this.choiceDao = choiceDao;
		this.choiceAnswerDao = choiceAnswerDao;
		this.questionDao = questionDao;
		this.questionAnswerDao = questionAnswerDao;
		this.enqueteDao = enqueteDao;
		this.enqueteAnswerDao = enqueteAnswerDao;
	}

	private void authorize(Integer enqueteId, String respondentEsqId) 
		throws ItemNotFoundException, UnauthorizedAccessException{
		Enquete enquete = enqueteDao.selectById(enqueteId);
		
		// アンケート回答対象者か確認
		List<EnqueteDept> enqueteDeptList = enqueteDeptDao.selectAllByEnqueteId(enqueteId);
		List<Integer> tgtDeptIdList = enqueteDeptList.stream()
				.map(enqueteDept -> enqueteDept.getDeptId())
				.toList();
		EsqUserInfo user = esqUserInfoDao.selectById(respondentEsqId);
		Integer deptId = user.getDeptId();
		
		if(!tgtDeptIdList.contains(deptId)) {
			throw new UnauthorizedAccessException("リクエストしたアンケートの管理者権限がありません。");
		}
		if(enquete == null) {
			throw new ItemNotFoundException("リクエストしたアンケートは存在しません。");
		}
	}

	@Override
	public AnswerEnqueteDto getEnqueteAnswer(
			TopRespondentEnqueteDto requestEnqDto, String esqId) 
			throws ItemNotFoundException, UnauthorizedAccessException {
		// リクエスト情報から enqueteId を取得
		RespondentEnquete requestEnq = requestEnqDto.getRespondentEnquete();
		Integer enqueteId = requestEnq.getEnqueteid();
		
		// 認証 (失敗の場合は例外がスロー)
		authorize(enqueteId, esqId);
		
		// アンケート回答用情報の格納 (アンケート情報, 回答情報)
		AnswerEnqueteDto answerEnqueteDto = new AnswerEnqueteDto();
		// アンケート情報の格納
		Enquete enquete = enqueteDao.selectById(enqueteId);
		answerEnqueteDto.setEnquete(enquete);
		
		// 保存された回答の有無を確認
		EnqueteAnswer tgtEnqueteAnswer = null;
		List<EnqueteAnswer> enqueteAnswerList = enqueteAnswerDao.selectAllByEnqueteId(enqueteId);
		for(EnqueteAnswer enqueteAnswer: enqueteAnswerList) {
			if(esqId.equals(enqueteAnswer.getEsqId())) {
				tgtEnqueteAnswer = enqueteAnswer;
				break;
			}
		}
		
		// 回答情報の格納
		List<Question> questionList = questionDao.selectAllByEnqueteId(enqueteId);
		List<QuestionItem> questionItemList = new ArrayList<>();

		for(Question question: questionList) {
			Integer questionId = question.getQuestionId();
			List<Choice> choiceList = choiceDao.selectAllByQuestionId(questionId);
			
			QuestionItem questionItem = new QuestionItem(question, choiceList);
			// 選択肢は React にて昇順処理を行わないため, こちらで番号順にソート
			questionItem.sortChoiceListByChoiceNumber();
			questionItemList.add(questionItem);
		}

		// 保存された回答情報がある場合, 回答情報をDBから取得して付与
		if(tgtEnqueteAnswer != null) {
			Integer enqueteAnswerId = tgtEnqueteAnswer.getEnqueteAnswerId();
			List<QuestionAnswer> questionAnswerList = 
					questionAnswerDao.selectAllByEnqueteAnswerId(enqueteAnswerId);
			
			for(QuestionItem questionItem: questionItemList) {
				Question question = questionItem.getQuestion();
				Integer questionId = question.getQuestionId();
				int typeId = question.getQuestionTypeId().intValue();
				ArrayList<Object> answer = new ArrayList<>();
				// 選択された質問の回答があるか確認
				QuestionAnswer questionAnswer = null;
				for(QuestionAnswer qAns: questionAnswerList) {
					Integer qAnsId = qAns.getQuestionId();
					if(qAnsId.equals(questionId)) {
						questionAnswer = qAns;
					}
				}
				
				if(questionAnswer != null) {
					// 自由記述
					if(typeId == 3) {
						answer.add(questionAnswer.getAnswerText());
					// 選択式
					}else {
						List<ChoiceAnswer> choiceAnswerList = 
								choiceAnswerDao.selectAllByQuestionAnswerId(questionId);
						List<Choice> choiceList = questionItem.getChoiceList();
						for(ChoiceAnswer choiceAnswer: choiceAnswerList) {
							Integer tgtChoiceId = choiceAnswer.getChoiceId();
							for(Choice choice: choiceList) {
								if(choice.getChoiceId().equals(tgtChoiceId)) {
									answer.add(choice.getChoiceNumber());
								}
							}
						}	
					}
				} 
				Object[] answerArray = answer.toArray();
				AnswerItem answerItem = new AnswerItem(questionItem, answerArray);
				answerEnqueteDto.addAnswerItem(answerItem);
			}
		// 回答が存在しない場合, 質問と空の配列が格納されたAnswerItemが取得できる
		}else {
			for(QuestionItem questionItem: questionItemList) {
				Object[] answerArray = new Object[0];
				AnswerItem answerItem = new AnswerItem(questionItem, answerArray);
				answerEnqueteDto.addAnswerItem(answerItem);
			}
		}
		return answerEnqueteDto;
	}
	
	@Override
	public void saveEnqueteAnswer(
			AnswerEnqueteDto answerEnqueteDto, String esqId) 
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException;
	
	@Override
	public void completeEnqueteAnswer(
			AnswerEnqueteDto answerEnqueteDto, String esqId) 
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException;
	
}
