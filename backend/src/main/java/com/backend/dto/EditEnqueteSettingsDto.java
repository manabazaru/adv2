package com.backend.dto;

import java.io.Serializable;
import java.util.List;

import com.backend.entity.Dept;
import com.backend.entity.Enquete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditEnqueteSettingsDto implements Serializable {
	
	private Enquete enquete;
	private boolean notifyFlag;
	private List<AdminUser> adminUserList;
	private List<DeptUser> allUserList;
	private List<Dept> targetDeptList;
	private List<Dept> allDeptList;
}
