package com.backend.dto;

import java.io.Serializable;
import java.util.List;

import com.backend.entity.AdminEnquete;
import com.backend.entity.RespondentEnquete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopDto implements Serializable{
	
	private List<AdminEnquete> publishedAdminEnqueteList;
	private List<AdminEnquete> unpublishedAdminEnqueteList;
	private List<AdminEnquete> closedAdminEnqueteList;
	private List<RespondentEnquete> incompletedAnswerEnqueteList;
	private List<RespondentEnquete> completedAnswerEnqueteList;
	private List<RespondentEnquete> expiredAnswerEnqueteList;
}
