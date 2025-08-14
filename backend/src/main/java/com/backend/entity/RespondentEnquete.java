package com.backend.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespondentEnquete {
	private Integer enqueteid;
	private String enqueteName;
	private String enqueteSubtext;
	private Date createDate;
	private Date startDate;
	private Date finishDate;
	private String adminUserId;
	private String createUserName;
	private String deptName;
}
