package com.backend.dto;

import java.io.Serializable;

import com.backend.entity.RespondentEnquete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopRespondentEnqueteDto implements Serializable {
	private RespondentEnquete respondentEnquete;
}
