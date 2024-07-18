package com.acsendo.api.customreports.dto;

import java.util.List;

public class CompareResultsDTO {
	
	private List<ResultDTO> primaryResults;

	private List<ResultDTO> secondaryResults;

	
	public List<ResultDTO> getPrimaryResults() {
		return primaryResults;
	}

	public void setPrimaryResults(List<ResultDTO> primaryResults) {
		this.primaryResults = primaryResults;
	}

	public List<ResultDTO> getSecondaryResults() {
		return secondaryResults;
	}

	public void setSecondaryResults(List<ResultDTO> secondaryResults) {
		this.secondaryResults = secondaryResults;
	}
	
}
