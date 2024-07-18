package com.acsendo.api.customreports.dto;

import java.util.List;

public class ProcessDTO {
	
	private Long processId;

	private List<PerformanceCategoryDTO> categories;
	

	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public List<PerformanceCategoryDTO> getCategories() {
		return categories;
	}

	public void setCategories(List<PerformanceCategoryDTO> categories) {
		this.categories = categories;
	}

}
