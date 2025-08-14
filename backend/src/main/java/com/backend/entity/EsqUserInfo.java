package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsqUserInfo {
	private String esqId;
	private String userName;
	private Integer deptId;
	
}
