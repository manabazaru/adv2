package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsqUser {
	private String esqId;
	private Integer deptId;
	private String userName;
	private String password;
}
