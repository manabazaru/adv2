package com.backend.dto;

import java.io.Serializable;

import com.backend.entity.EsqUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest implements Serializable {
	private EsqUser esqUser;
}
