package com.backend.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminEnquete {
	private Integer enqueteId;
	private String enqueteName;
	private String enqueteSubtext;
	private Integer enqueteStateId;
	private Date createDate;
	private Date startDate;
	private Date finishDate;
	private String adminUserId;
	private String createUserName;
}
