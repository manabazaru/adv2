package com.backend.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.dao.ChoiceDao;
import com.backend.dao.EnqueteAdminUserDao;
import com.backend.dao.EnqueteDao;
import com.backend.dao.QuestionDao;
import com.backend.dto.EditEnqueteContentDto;
import com.backend.dto.QuestionItem;
import com.backend.dto.TopAdminEnqueteDto;
import com.backend.entity.AdminEnquete;
import com.backend.entity.Choice;
import com.backend.entity.Enquete;
import com.backend.entity.EnqueteAdminUser;
import com.backend.entity.Question;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.SystemException;
import com.backend.exception.UnauthorizedAccessException;

@Service
public class EditEnqueteContentsServiceImpl implements EditEnqueteContentsService{
	private ChoiceDao choiceDao;
	private QuestionDao questionDao;
	private EnqueteDao enqueteDao;
	private EnqueteAdminUserDao enqueteAdminUserDao;
	
	public EditEnqueteContentsServiceImpl(
			ChoiceDao choiceDao,
			QuestionDao questionDao,
			EnqueteDao enqueteDao,
			EnqueteAdminUserDao enqueteAdminUserDao
			) {
		this.choiceDao = choiceDao;
		this.questionDao = questionDao;
		this.enqueteDao = enqueteDao;
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
	public EditEnqueteContentDto getCopiedEnqueteContents(
		TopAdminEnqueteDto requestEnqDto, String adminEsqId)
		throws ItemNotFoundException, UnauthorizedAccessException{
		// リクエスト情報から enqueteId を取得
		AdminEnquete requestEnq = requestEnqDto.getAdminEnquete();
		Integer enqueteId = requestEnq.getEnqueteId();

		// 認証 (失敗の場合は例外がスロー)
		authorize(enqueteId, adminEsqId);
		
		// 編集用アンケート情報の設定 (アンケート情報, 質問情報)
		EditEnqueteContentDto editEnqueteContentDto = new EditEnqueteContentDto();
		// アンケート情報の格納 (複製のため null )
		editEnqueteContentDto.setEnquete(null);
		
		// 質問情報の格納
		List<Question> questionList = questionDao.selectAllByEnqueteId(enqueteId);
		
		for(Question question: questionList) {
			Integer questionId = question.getQuestionId();
			List<Choice> choiceList = choiceDao.selectAllByQuestionId(questionId);
			
			QuestionItem questionItem = new QuestionItem(question, choiceList);
			// 選択肢は React にて昇順処理を行わないため, こちらで番号順にソート
			questionItem.sortChoiceListByChoiceNumber();
			editEnqueteContentDto.addQuestionItem(questionItem);
		}
		
		// 選択肢は React にて昇順処理を行わないため, こちらで番号順にソート
		editEnqueteContentDto.sortQuestionListByQuestionNumber();
		
		return editEnqueteContentDto;
		
	}
	
	@Override
	public EditEnqueteContentDto getSavedEnqueteContents(
		TopAdminEnqueteDto requestEnqDto, String adminEsqId)
		throws ItemNotFoundException, UnauthorizedAccessException{
		// リクエスト情報から enqueteId を取得
		AdminEnquete requestEnq = requestEnqDto.getAdminEnquete();
		Integer enqueteId = requestEnq.getEnqueteId();
		Enquete enquete = enqueteDao.selectById(enqueteId);

		// 認証 (失敗の場合は例外がスロー)
		authorize(enqueteId, adminEsqId);
		
		// 編集用アンケート情報の設定 (アンケート情報, 質問情報)
		EditEnqueteContentDto editEnqueteContentDto = new EditEnqueteContentDto();
		// アンケート情報の格納
		editEnqueteContentDto.setEnquete(enquete);
		
		// 質問情報の格納
		List<Question> questionList = questionDao.selectAllByEnqueteId(enqueteId);
		
		for(Question question: questionList) {
			Integer questionId = question.getQuestionId();
			List<Choice> choiceList = choiceDao.selectAllByQuestionId(questionId);
			
			QuestionItem questionItem = new QuestionItem(question, choiceList);
			// 選択肢は React にて昇順処理を行わないため, こちらで番号順にソート
			questionItem.sortChoiceListByChoiceNumber();
			editEnqueteContentDto.addQuestionItem(questionItem);
		}
		
		// 選択肢は React にて昇順処理を行わないため, こちらで番号順にソート
		editEnqueteContentDto.sortQuestionListByQuestionNumber();
		
		return editEnqueteContentDto;
	}
	
	@Override
	public void saveEnqueteContents(
		EditEnqueteContentDto editEnqueteContentDto, String adminEsqId)
		throws ItemNotFoundException, UnauthorizedAccessException, SystemException{
		// アンケート情報を取得
		Enquete enquete = editEnqueteContentDto.getEnquete();
		Integer enqueteId = enquete.getEnqueteId();
		
		// アンケートが初めて保存される場合
		if(enqueteId < 1) {
			enquete.setCreateUserId(adminEsqId);
			enquete.setEnqueteStateId(1);
			enquete.setVersion(0);
			LocalDate localDate = LocalDate.now();
			Date startDate = Date.valueOf(localDate);
			enquete.setCreateDate(startDate);
			enqueteDao.insert(enquete);
			enqueteId = enquete.getEnqueteId();
		}else {
			// 認証 (失敗の場合は例外がスロー)
			authorize(enqueteId, adminEsqId);			
		}
		
		// 情報の更新
		// 保存前の質問情報をデータベースから取得
		List<Question> oldQuestionList = questionDao.selectAllByEnqueteId(enqueteId);
		List<Choice> oldChoiceList = new ArrayList<>();
		for(Question question: oldQuestionList) {
			Integer questionId = question.getQuestionId();
			List<Choice> choiceList = choiceDao.selectAllByQuestionId(questionId);
			oldChoiceList.addAll(choiceList);
		}
		// のちの比較のため, id順にソート
		oldQuestionList.sort(Comparator.comparing(question -> question.getQuestionId()));
		oldChoiceList.sort(Comparator.comparing(choice -> choice.getChoiceId()));
		
		// 保存内容を editEnqueteContentDto から取得
		List<Question> newQuestionList = new ArrayList<>();
		List<Choice> newChoiceList = new ArrayList<>();
		// 新しく追加された設問のみ格納
		List<QuestionItem> noIdQuestionItemList = new ArrayList<>();
		// のちの比較のため, id順にソート
		editEnqueteContentDto.sortQuestionListByQuestionId();
		List<QuestionItem> questionList = editEnqueteContentDto.getQuestionList();
		for(QuestionItem questionItem: questionList) {
			if(questionItem.getQuestion().getQuestionId()<1) {
				noIdQuestionItemList.add(questionItem);
				continue;
			}
			newQuestionList.add(questionItem.getQuestion());
			// のちの比較のため, id順にソート
			questionItem.sortChoiceListByChoiceId();
			List<Choice> choiceList = questionItem.getChoiceList();
			newChoiceList.addAll(choiceList);
		}
		
		// 比較 (DB削除/更新/挿入)
		// choice の比較 (比較はDBの外部キー制約があるため Choice)
		int oldCSize = oldChoiceList.size();
		int newCSize = newChoiceList.size();
		
		int oldCIdx = 0;
		int newCIdx = 0;
		
		while(true) {
			Choice oldChoice = oldCIdx < oldCSize ? oldChoiceList.get(oldCIdx) : null;
			Choice newChoice = newCIdx < newCSize ? newChoiceList.get(newCIdx) : null;
			
			// 新旧ともに全て確認し終わった場合
			if(oldChoice == null && newChoice == null) {
				break;
			}
			
			// 古い方のみを確認し終わった場合
			// 新規で追加するエンティティはidに-1が入っている
			// よって基本的に新しい方だけ残る事はなく, この場合はエラーを出力する
			if(oldChoice == null) {
				throw new SystemException("不適切にChoiceIdが設定されています。");
			}
			
			// 新しい方のみを確認し終わった場合
			// 古い方に残った値はすべて削除
			if(newChoice == null) {
				try {
					choiceDao.delete(oldChoice);
				// 例外名はdomaのversion管理例外に合わせる (OptimisticLockException?)
				}catch (Exception e) {
					throw new UnauthorizedAccessException("編集中に他の管理者から内容が更新されました。");
				}
				oldCIdx += 1;
				continue;
			}
			
			// 両方残っている場合
			// 選択中のnewChoice, oldChoiceのidを比較
			Integer oldChoiceId = oldChoice.getChoiceId();
			Integer newChoiceId = newChoice.getChoiceId();
			
			try {
				// newId < oldId: 新しいレコードがあるため newId の方を挿入
				if(newChoiceId < oldChoiceId) {
					choiceDao.insert(newChoice);
					newCIdx += 1;
					continue;
				}
				
				// newId = oldId: 既存のレコードが残っているため, 更新
				if(newChoiceId.intValue() == oldChoiceId.intValue()) {
					choiceDao.update(newChoice);
					newCIdx += 1;
					oldCIdx += 1;
					continue;
				}
				
				// newId > oldId: レコードが消えているため oldId の方を削除
				if(newChoiceId > oldChoiceId) {
					choiceDao.delete(oldChoice);
					oldCIdx += 1;
					continue;
				}	
			// 例外名はdomaのversion管理例外に合わせる (OptimisticLockException?)
			}catch (Exception e) {
				throw new SystemException(e.getMessage());
			}
		}
			
		// question の比較
		int oldQSize = oldQuestionList.size();
		int newQSize = newQuestionList.size();

		int oldQIdx = 0;
		int newQIdx = 0;

		while(true) {
			Question oldQuestion = oldQIdx < oldQSize ? oldQuestionList.get(oldQIdx) : null;
			Question newQuestion = newQIdx < newQSize ? newQuestionList.get(newQIdx) : null;
			
			// 新旧ともに全て確認し終わった場合
			if(oldQuestion == null && newQuestion == null) {
				break;
			}
			
			// 古い方のみを確認し終わった場合
			// 新規で追加するエンティティはidに-1が入っている
			// よって基本的に新しい方だけ残る事はなく, この場合はエラーを出力する
			if(oldQuestion == null) {
				throw new SystemException("不適切にquestionIdが設定されています。");
			}
			
			// 新しい方のみを確認し終わった場合
			// 古い方に残った値はすべて削除
			if(newQuestion == null) {
				try {
					questionDao.delete(oldQuestion);
				// 例外名はdomaのversion管理例外に合わせる (OptimisticLockException?)
				}catch (Exception e) {
					throw new UnauthorizedAccessException("編集中に他の管理者から内容が更新されました。");
				}
				oldQIdx += 1;
				continue;
			}
			
			// 両方残っている場合
			// 選択中のnewQuestion, oldQuestionのidを比較
			Integer oldQuestionId = oldQuestion.getQuestionId();
			Integer newQuestionId = newQuestion.getQuestionId();
			
			try {
				// newId < oldId: 新しいレコードがあるため newId の方を挿入
				if(newQuestionId < oldQuestionId) {
					questionDao.insert(newQuestion);
					newQIdx += 1;
					continue;
				}
				
				// newId = oldId: 既存のレコードが残っているため, 更新
				if(newQuestionId == oldQuestionId) {
					questionDao.update(newQuestion);
					newQIdx += 1;
					oldQIdx += 1;
					continue;
				}
				
				// newId > oldId: レコードが消えているため oldId の方を削除
				if(newQuestionId > oldQuestionId) {
					questionDao.delete(oldQuestion);
					oldQIdx += 1;
					continue;
				}	
			// 例外名はdomaのversion管理例外に合わせる (OptimisticLockException?)
			}catch (Exception e) {
				throw new SystemException(e.getMessage());
			}
		}
		 // 新しく追加された設問の挿入
		for(QuestionItem questionItem: noIdQuestionItemList) {
			Question question = questionItem.getQuestion();
			questionDao.insert(question);
			Integer questionId = question.getQuestionId();
			for(Choice choice: questionItem.getChoiceList()) {
				choice.setQuestionId(questionId);
				choiceDao.insert(choice);
			}
		}
	}
}
