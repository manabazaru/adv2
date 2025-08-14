package com.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnqueteAdminUser {
	private Integer enqueteId;
	private String esqId;
	private Integer deleteFlag;
}
