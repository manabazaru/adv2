package com.backend.dto;

import java.io.Serializable;
import java.util.List;

import com.backend.entity.Dept;
import com.backend.entity.EsqUserInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeptAnswer implements Serializable {
	private Dept dept;
	private List<EsqUserInfo> esqUserList;
	private List<Boolean> hasResponseList;
	private List<List<UserAnswerItem>> answerList;
	
	public void addUserAnswerItem(List<UserAnswerItem> userAnswerItemList) {
		answerList.add(userAnswerItemList);
	}
}
