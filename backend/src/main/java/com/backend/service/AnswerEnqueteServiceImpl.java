package com.backend.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
			throw new UnauthorizedAccessException("リクエストしたアンケートの回答者権限がありません。");
		}
		if(enquete == null) {
			throw new ItemNotFoundException("リクエストしたアンケートは存在しません。");
		}
		// アンケートが回答可能でない場合
		if(!enquete.getEnqueteStateId().equals(Integer.valueOf(3))) {
			throw new UnauthorizedAccessException("リクエストしたアンケートは期限切れです。");
		}
	}

	private void authorizeForPreview(Integer enqueteId, String respondentEsqId) 
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
				throw new UnauthorizedAccessException("リクエストしたアンケートの回答者権限がありません。");
			}
			if(enquete == null) {
				throw new ItemNotFoundException("リクエストしたアンケートは存在しません。");
			}
			// アンケートが公開前の場合
			boolean isEnquetePublished = 
					enquete.getEnqueteStateId().equals(Integer.valueOf(3));
			boolean isEnqueteFinished = 
					enquete.getEnqueteStateId().equals(Integer.valueOf(4));
			if(!isEnquetePublished && !isEnqueteFinished) {
				throw new UnauthorizedAccessException("リクエストしたアンケートは期限切れです。");
			}
		}	

	@Override
	public AnswerEnqueteDto getEnqueteAnswer(
			TopRespondentEnqueteDto requestEnqDto, String esqId, boolean isReadOnly) 
			throws ItemNotFoundException, UnauthorizedAccessException {
		// リクエスト情報から enqueteId を取得
		RespondentEnquete requestEnq = requestEnqDto.getRespondentEnquete();
		Integer enqueteId = requestEnq.getEnqueteid();
		
		// 認証 (失敗の場合は例外がスロー)
		if(isReadOnly) {
			authorizeForPreview(enqueteId, esqId);
		}else {
			authorize(enqueteId, esqId);
		}
		
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
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException{
		// Dtoの情報から enqueteId を取得
		Integer enqueteId = answerEnqueteDto.getEnquete().getEnqueteId();
		
		// 認証 (失敗の場合は例外がスロー)
		authorize(enqueteId, esqId);
		
		// 保存された回答の有無を確認
		EnqueteAnswer enqueteAnswer = null;
		List<EnqueteAnswer> enqueteAnswerList = enqueteAnswerDao.selectAllByEnqueteId(enqueteId);
		for(EnqueteAnswer filteringEnqueteAnswer: enqueteAnswerList) {
			if(esqId.equals(filteringEnqueteAnswer.getEsqId())) {
				enqueteAnswer = filteringEnqueteAnswer;
				break;
			}
		}
		// 初めて保存する場合, 値を新たに格納
		if(enqueteAnswer == null) {
			enqueteAnswer = new EnqueteAnswer();
			enqueteAnswer.setEnqueteId(enqueteId);
			enqueteAnswer.setEsqId(esqId);
			enqueteAnswer.setCompletedFlag(0);
			enqueteAnswerDao.insert(enqueteAnswer);
		}
		// 保存された回答がすでに完了ステータスの場合 (例外処理)
		if(enqueteAnswer.getCompletedFlag().equals(Integer.valueOf(1))){
			throw new UnauthorizedAccessException("すでに回答済みです");
		}
		
		// 回答の更新
		record AnswerData (QuestionAnswer questionAnswer, List<ChoiceAnswer> choiceAnswerList) {};
		Integer enqueteAnswerId = enqueteAnswer.getEnqueteAnswerId();
		// 保存前の回答情報をデータベースから取得
		List<QuestionAnswer> oldQuestionAnswerList = 
				questionAnswerDao.selectAllByEnqueteAnswerId(enqueteAnswerId);
		List<AnswerData> oldAnswerDataList = new ArrayList<>();
		for(QuestionAnswer qAns: oldQuestionAnswerList) {
			Integer qAnsId = qAns.getQuestionAnswerId();
			List<ChoiceAnswer> choiceAnswerList = new ArrayList<>();
			choiceAnswerList = choiceAnswerDao.selectAllByQuestionAnswerId(qAnsId);
			AnswerData ansData = new AnswerData(qAns, choiceAnswerList);
			oldAnswerDataList.add(ansData);
		}

		// 保存内容をDtoから取得
		List<AnswerData> newAnswerDataList = new ArrayList<>();
		// のちの比較のため, id順にソート
		answerEnqueteDto.sortAnswerListByQuestionId();
		List<AnswerItem> answerItemList = answerEnqueteDto.getAnswerList();
		for(AnswerItem answerItem: answerItemList) {
			QuestionItem questionItem = answerItem.getQuestionItem();
			Object answer = answerItem.getAnswer();
			Question question = questionItem.getQuestion();
			int questionTypeId = question.getQuestionTypeId().intValue();
			
			QuestionAnswer newQAns = new QuestionAnswer();
			newQAns.setEnqueteAnswerId(enqueteAnswerId);
			newQAns.setQuestionId(question.getQuestionId());
			// 自由記述
			if(questionTypeId == 3) {
				newQAns.setAnswerText((String)answer);
				AnswerData descAnsData = new AnswerData(newQAns, null);
				newAnswerDataList.add(descAnsData);
				continue;
			}
			List<Choice> choiceList = questionItem.getChoiceList();
			List<ChoiceAnswer> cAnsList = new ArrayList<>();
			// 複数選択
			if(questionTypeId == 2){
				List<Integer> answers = (List<Integer>) answer;
				for(Integer ansId: answers) {
					for(Choice choice: choiceList) {
						Integer choiceNumber = choice.getChoiceNumber();
						if(choiceNumber.equals(ansId)) {
							ChoiceAnswer choiceAnswer = new ChoiceAnswer();
							choiceAnswer.setChoiceId(choice.getChoiceId());
							cAnsList.add(choiceAnswer);
						}
					}
				}
				AnswerData sChoiceAnsData = new AnswerData(newQAns, cAnsList);
				newAnswerDataList.add(sChoiceAnsData);
			}else {
				for(Choice choice: choiceList) {
					Integer choiceNumber = choice.getChoiceNumber();
					if(choiceNumber.equals((Integer)answer)) {
						ChoiceAnswer choiceAnswer = new ChoiceAnswer();
						choiceAnswer.setChoiceId(choice.getChoiceId());
						cAnsList.add(choiceAnswer);
						break;
					}
				}
			}
			AnswerData mChoiceAnsData = new AnswerData(newQAns, cAnsList);
			newAnswerDataList.add(mChoiceAnsData);
		}
		
		// 比較
		// QuestionAnswer の更新&挿入をanswerDataごとに行い, 
		// 最後に既存にある消去されたQuestionAnswerを削除
		boolean[] deleteQAFlags = new boolean[oldQuestionAnswerList.size()];
		Arrays.fill(deleteQAFlags, true);

		for(AnswerData newAns: newAnswerDataList) {
			QuestionAnswer newQAns = newAns.questionAnswer;
			List<ChoiceAnswer> newCAnsList = newAns.choiceAnswerList();
			boolean isQAUpdate = false;
			for(int oldAnsIdx = 0; oldAnsIdx < oldAnswerDataList.size(); oldAnsIdx++) {
				AnswerData oldAns = oldAnswerDataList.get(oldAnsIdx);
				QuestionAnswer oldQAns = oldAns.questionAnswer;
				List<ChoiceAnswer> oldCAnsList = oldAns.choiceAnswerList;
				
				boolean isQuestionIdEqual = 
						newQAns.getQuestionId().equals(oldQAns.getQuestionId());
				if(isQuestionIdEqual) {
					isQAUpdate = true;
					deleteQAFlags[oldAnsIdx] = false;
					newQAns.setQuestionAnswerId(oldQAns.getQuestionAnswerId());
					questionAnswerDao.update(newQAns);
					Integer questionAnswerId = newQAns.getQuestionAnswerId();
					
					// 選択式の場合, choiceAnswerの更新/挿入/削除
					// 変更前後でどちらも未回答の選択式ものぞく
					boolean isFreeDescription = 
							newCAnsList.size() == 0 && oldCAnsList.size() == 0;
					if(!isFreeDescription) {
						boolean[] deleteCAFlags = new boolean[oldCAnsList.size()];
						boolean[] insertCAFlags = new boolean[newCAnsList.size()];
						Arrays.fill(deleteQAFlags, true);
						Arrays.fill(insertCAFlags, true);
						for(ChoiceAnswer newCAns: newCAnsList) {
							boolean isCAUpdate = false;
							newCAns.setQuestionAnswerId(questionAnswerId);
							for(int oldCIdx = 0 ; oldCIdx < oldCAnsList.size() ; oldCIdx++) {
								ChoiceAnswer oldCAns = oldCAnsList.get(oldCIdx);
								boolean isChoiceIdEqual = 
										newCAns.getChoiceId().equals(oldCAns.getChoiceId());
								if(isChoiceIdEqual) {
									isCAUpdate = true;
									deleteCAFlags[oldCIdx] = false;
									newCAns.setChoiceAnswerId(oldCAns.getChoiceAnswerId());
									choiceAnswerDao.update(newCAns);
								}
							}
							if(!isCAUpdate) {
								choiceAnswerDao.insert(newCAns);
							}
						}
						for(int oldCIdx = 0; oldCIdx < oldCAnsList.size(); oldCIdx++) {
							ChoiceAnswer oldCAns = oldCAnsList.get(oldCIdx);
							if(deleteCAFlags[oldCIdx]) {
								choiceAnswerDao.delete(oldCAns);
							}
						}
					}
				}	
			}
			if(!isQAUpdate) {
				questionAnswerDao.insert(newQAns);
			}
		}
		for(int oldAnsIdx = 0; oldAnsIdx < oldAnswerDataList.size(); oldAnsIdx++) {
			AnswerData oldAns = oldAnswerDataList.get(oldAnsIdx);
			QuestionAnswer oldQAns = oldAns.questionAnswer;
			if(deleteQAFlags[oldAnsIdx]) {
				questionAnswerDao.delete(oldQAns);
			}
		}
	}
	
	@Override
	public void completeEnqueteAnswer(
		AnswerEnqueteDto answerEnqueteDto, String esqId) 
		throws ItemNotFoundException, UnauthorizedAccessException, SystemException{
		
		// 回答の保存
		saveEnqueteAnswer(answerEnqueteDto, esqId);
		
		Enquete enquete = answerEnqueteDto.getEnquete();
		Integer enqueteId = enquete.getEnqueteId();
		
		// 保存された回答を取得
		EnqueteAnswer enqueteAnswer = null;
		List<EnqueteAnswer> enqueteAnswerList = enqueteAnswerDao.selectAllByEnqueteId(enqueteId);
		for(EnqueteAnswer filteringEnqueteAnswer: enqueteAnswerList) {
			if(esqId.equals(filteringEnqueteAnswer.getEsqId())) {
				enqueteAnswer = filteringEnqueteAnswer;
				break;
			}
		}
		// 回答が取得できない場合
		if(enqueteAnswer == null) {
			throw new SystemException("回答が正常に保存されませんでした");
		}
		
		// enqueteAnswerの必要事項を格納
		LocalDate localDate = LocalDate.now();
		Date answerDate = Date.valueOf(localDate);
		enqueteAnswer.setAnswerDate(answerDate);
		enqueteAnswer.setCompletedFlag(1);
		
		enqueteAnswerDao.update(enqueteAnswer);
	}
	
}
