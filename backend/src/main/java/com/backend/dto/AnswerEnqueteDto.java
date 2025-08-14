package com.backend.dto;

import java.io.Serializable;
import java.util.List;

import com.backend.entity.Enquete;
import com.backend.entity.EsqUserInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerEnqueteDto implements Serializable {
	private Enquete enquete;
	private EsqUserInfo respondentEsqId;
	private List<AnswerItem> answerList;
}
