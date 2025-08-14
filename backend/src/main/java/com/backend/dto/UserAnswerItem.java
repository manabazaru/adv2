package com.backend.dto;

import java.io.Serializable;

import com.backend.entity.EsqUserInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerItem implements Serializable {
	private EsqUserInfo ansUserInfo;
	private Object answer;
}
