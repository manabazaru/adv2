package com.backend.dto;

import java.io.Serializable;

import com.backend.entity.AdminEnquete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopAdminEnqueteDto implements Serializable{
	private AdminEnquete adminEnquete;
}
