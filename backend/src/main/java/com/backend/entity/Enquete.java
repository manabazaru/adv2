package com.backend.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Enquete {
	private Integer enqueteId;
	private String enqueteName;
	private Integer enqueteStateId;
	private String createUserId;
	private Date createDate;
	private Date startDate;
	private Date finishDate;
	private String enqueteSubtext;
	private String thxComment;
	private Integer version;
}
