package com.backend.dto;

import java.io.Serializable;

import com.backend.entity.Enquete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnqueteDto implements Serializable {
	private Enquete enquete;
}
