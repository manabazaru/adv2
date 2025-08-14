package com.backend.dto;

import java.io.Serializable;

import com.backend.entity.EsqUserInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeptUser implements Serializable{
	private EsqUserInfo esqUserInfo;
	private String deptName;
}
