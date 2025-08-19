package com.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.dao.DeptDao;
import com.backend.dao.EnqueteAdminUserDao;
import com.backend.dao.EnqueteDao;
import com.backend.dao.EnqueteDeptDao;
import com.backend.dao.EsqUserInfoDao;
import com.backend.dto.AdminUser;
import com.backend.dto.DeptUser;
import com.backend.dto.EditEnqueteSettingsDto;
import com.backend.dto.EnqueteDto;
import com.backend.dto.TopAdminEnqueteDto;
import com.backend.entity.Dept;
import com.backend.entity.Enquete;
import com.backend.entity.EnqueteAdminUser;
import com.backend.entity.EnqueteDept;
import com.backend.entity.EsqUserInfo;
import com.backend.exception.ItemNotFoundException;
import com.backend.exception.SystemException;
import com.backend.exception.UnauthorizedAccessException;

@Service
public class EditEnqueteSettingsServiceImpl implements EditEnqueteSettingsService{
	private EsqUserInfoDao esqUserInfoDao;
	private DeptDao deptDao;
	private EnqueteDao enqueteDao;
	private EnqueteDeptDao enqueteDeptDao;
	private EnqueteAdminUserDao enqueteAdminUserDao;
	
	public EditEnqueteSettingsServiceImpl (
		EsqUserInfoDao esqUserInfoDao,
		DeptDao deptDao,
		EnqueteDao enqueteDao,
		EnqueteAdminUserDao enqueteAdminUserDao
		) {
		this.esqUserInfoDao = esqUserInfoDao;
		this.deptDao = deptDao;
		this.enqueteDao = enqueteDao;
		this.enqueteAdminUserDao = enqueteAdminUserDao;
	}

	private void authorize(Integer enqueteId, String adminEsqId) 
			throws ItemNotFoundException, UnauthorizedAccessException{
			Enquete enquete = enqueteDao.selectById(enqueteId);
			
			EnqueteAdminUser adminUser = enqueteAdminUserDao.selectById(enqueteId, adminEsqId);
			if(adminUser == null || adminUser.getDeleteFlag().equals(Integer.valueOf(1))) {
				throw new UnauthorizedAccessException("リクエストしたアンケートの管理者権限がありません。");
			}
			if(enquete == null) {
				throw new ItemNotFoundException("リクエストしたアンケートは存在しません。");
			}
		}
	
	private EditEnqueteSettingsDto getSavedEnqueteSettings(Enquete enquete, String esqId)
		throws ItemNotFoundException, UnauthorizedAccessException, SystemException{
		Integer enqueteId = enquete.getEnqueteId();

		// 認証 (失敗の場合は例外がスロー)
		authorize(enqueteId, esqId);
		
		// dto の設定
		EditEnqueteSettingsDto dto = new EditEnqueteSettingsDto();
		dto.setNotifyFlag(false);
		dto.setEnquete(enquete);
		
		// 全ての事業部取得・格納
		List<Dept> allDeptList = new ArrayList<>();
		int deptCnt = 1;
		while(true) {
			Dept dept = deptDao.selectById(Integer.valueOf(deptCnt));
			if(dept == null) {break;}
			allDeptList.add(dept);
			deptCnt += 1;
		}
		dto.setAllDeptList(allDeptList);
		
		// 全ユーザの取得・格納
		List<DeptUser> allUserList = new ArrayList<>();
		List<EsqUserInfo> allEsqUserInfoList = esqUserInfoDao.selectAll();
		for(EsqUserInfo esqUserInfo: allEsqUserInfoList) {
			DeptUser user = new DeptUser();
			Integer deptId = esqUserInfo.getDeptId();
			String deptName = deptDao.selectById(deptId).getDeptName();
			user.setDeptName(deptName);
			user.setEsqUserInfo(esqUserInfo);
			allUserList.add(user);
		}
		dto.setAllUserList(allUserList);
		
		// 管理者ユーザの取得・格納
		List<AdminUser> adminUserList = new ArrayList<>();
		// 管理者の取得
		List<EnqueteAdminUser> enqueteAdminUserList = enqueteAdminUserDao.selectAllByEnqueteId(enqueteId);
		for(EnqueteAdminUser enqueteAdminUser: enqueteAdminUserList) {
			// deleteFlagの判定
			if(enqueteAdminUser.getDeleteFlag().equals(Integer.valueOf(1))) {
				continue;
			}
			String adminEsqId = enqueteAdminUser.getEsqId();
			EsqUserInfo adminInfo = esqUserInfoDao.selectById(adminEsqId);
			String adminDeptName = 
				deptDao.selectById(adminInfo.getDeptId()).getDeptName();
			// 作成者か判定
			String role = enquete.getCreateUserId().equals(adminEsqId) ? "作成者" : "管理者";
			AdminUser user = new AdminUser(new DeptUser(adminInfo, adminDeptName), role);
			adminUserList.add(user);
		}
		
		// アンケート対象事業部の取得・格納
		List<Dept> tgtDeptList = new ArrayList<>();
		List<EnqueteDept> enqueteDeptList = enqueteDeptDao.selectAllByEnqueteId(enqueteId);
		for(EnqueteDept enqueteDept: enqueteDeptList) {
			Integer deptId = enqueteDept.getDeptId();
			Dept dept = deptDao.selectById(deptId);
			tgtDeptList.add(dept);
		}
		dto.setTargetDeptList(tgtDeptList);
		return dto;
	}
	
	public EditEnqueteSettingsDto getSavedEnqueteSettings(
			EnqueteDto enqueteDto,String esqId)
			throws ItemNotFoundException, UnauthorizedAccessException, SystemException{
		
		// リクエスト情報から enqueteId を取得
		Enquete enquete = enqueteDto.getEnquete();
		return getSavedEnqueteSettings(enquete, esqId);
		
	}

	
	public EditEnqueteSettingsDto getSavedEnqueteSettings(
		TopAdminEnqueteDto enqueteDto,String esqId)
		throws ItemNotFoundException, UnauthorizedAccessException, SystemException{
		
		// リクエスト情報から enqueteId を取得
		Integer enqueteId = enqueteDto.getAdminEnquete().getEnqueteId();
		Enquete enquete = enqueteDao.selectById(enqueteId);
		return getSavedEnqueteSettings(enquete, esqId);
	}

	
	public void notifyTeams(EnqueteDto enqueteDto, String esqId)
		throws ItemNotFoundException, UnauthorizedAccessException, SystemException{
		
	}
	
	public void saveEnqueteSettings(
		EditEnqueteSettingsDto editEnqueteSettingsDto, String esqId)
		throws ItemNotFoundException, UnauthorizedAccessException, SystemException{
		
		// アンケート情報を取得
		Enquete enquete = editEnqueteSettingsDto.getEnquete();
		Integer enqueteId = enquete.getEnqueteId();
		
		// 管理者情報の更新
		// 保存前の管理者リストをDBから取得
		List<EsqUserInfo> oldUserList = new ArrayList<>();
		List<EnqueteAdminUser> oldEnqueteAdminUserList = 
				enqueteAdminUserDao.selectAllByEnqueteId(enqueteId);
		for(EnqueteAdminUser enqueteAdminUser: oldEnqueteAdminUserList) {
			String oldEsqId = enqueteAdminUser.getEsqId();
			EsqUserInfo oldUser = esqUserInfoDao.selectById(oldEsqId);
			oldUserList.add(oldUser);		
		}
		String authorId = enquete.getCreateUserId();
		
		// 保存する管理者リストをDtoから取得
		List<AdminUser> adminUserList = editEnqueteSettingsDto.getAdminUserList();
		List<EsqUserInfo> newUserList = adminUserList.stream()
			.map(adminUser -> adminUser.getDeptUser().getEsqUserInfo())
			.toList();
		
		// 比較
		// 更新・挿入ののち削除
		// 既存データの削除フラグリスト
		boolean[] deleteUserFlags = new boolean[oldUserList.size()];
		Arrays.fill(deleteUserFlags, true);
		for(EsqUserInfo newUser: newUserList) {
			boolean isUpdate = false;
			for(int oldUserIdx = 0; oldUserIdx < oldUserList.size(); oldUserIdx++) {
				EsqUserInfo oldUser = oldUserList.get(oldUserIdx);
				EnqueteAdminUser oldEnqueteAdminUser = oldEnqueteAdminUserList.get(oldUserIdx);
				boolean isEsqIdEqual = newUser.getEsqId().equals(oldUser.getEsqId());
				boolean hasDeleted = 
						oldEnqueteAdminUser.getDeleteFlag().equals(Integer.valueOf(1));
				// 削除フラグが立っていたユーザへ管理者権限付与
				if(isEsqIdEqual && hasDeleted) {
					oldEnqueteAdminUser.setDeleteFlag(0);
					enqueteAdminUserDao.update(oldEnqueteAdminUser);
				}
				if(isEsqIdEqual) {
					isUpdate = true;
					deleteUserFlags[oldUserIdx] = false;
					break;
				}
			}
			// テーブルに管理者として登録されていない場合, 挿入
			if(!isUpdate) {
				EnqueteAdminUser newAdminUser = new EnqueteAdminUser();
				newAdminUser.setEnqueteId(enqueteId);
				newAdminUser.setEsqId(newUser.getEsqId());
				newAdminUser.setDeleteFlag(0);
				enqueteAdminUserDao.insert(newAdminUser);
			}
		}
		// 保存する管理者リストにないユーザの削除
		for(int oldUserIdx = 0; oldUserIdx < oldUserList.size(); oldUserIdx++) {
			EnqueteAdminUser oldEnqueteAdminUser = oldEnqueteAdminUserList.get(oldUserIdx);
			boolean hasDeleted = 
					oldEnqueteAdminUser.getDeleteFlag().equals(Integer.valueOf(1));
			boolean isAuthor = 
					oldEnqueteAdminUser.getEsqId().equals(authorId);
			// 作成者が管理者リストにない場合, 例外処理
			if(isAuthor) {
				throw new SystemException("作成者が削除されています。");
			}
			// 削除フラグを1に変更
			if(deleteUserFlags[oldUserIdx] && !hasDeleted) {
				oldEnqueteAdminUser.setDeleteFlag(1);
				enqueteAdminUserDao.update(oldEnqueteAdminUser);
			}
		}
		
		// 公開事業部の変更
		// 保存前の事業部をDBから取得
		List<Dept> oldDeptList = new ArrayList<>();
		List<EnqueteDept> enqueteDeptList = enqueteDeptDao.selectAllByEnqueteId(enqueteId);
		for(EnqueteDept enqueteDept: enqueteDeptList) {
			Integer deptId = enqueteDept.getDeptId();
			Dept dept = deptDao.selectById(deptId);
			oldDeptList.add(dept);
		}
		
		// 保存する事業部をDtoから取得
		List<Dept> newDeptList = editEnqueteSettingsDto.getTargetDeptList();
		
		// 比較
		// 既存のDBにないものを追加
		for(Dept newDept: newDeptList) {
			boolean isSaved = false;
			for(Dept oldDept: oldDeptList) {
				if(newDept.getDeptId().equals(oldDept.getDeptId())) {
					isSaved = true;
					break;
				}
			}
			if(!isSaved) {
				EnqueteDept enqueteDept = new EnqueteDept();
				enqueteDept.setDeptId(newDept.getDeptId());
				enqueteDept.setEnqueteid(enqueteId);
				enqueteDeptDao.insert(enqueteDept);
			}
		}
		
		
	}
	
}
