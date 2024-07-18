package com.acsendo.api.customreports.dto;

import java.util.List;

public class CompareEvaluationsExcelDTO {
	
	private String evaluationName;
	
	private List<ResultDTO> results;

	public String getEvaluationName() {
		return evaluationName;
	}

	public List<ResultDTO> getResults() {
		return results;
	}

	public void setEvaluationName(String evaluationName) {
		this.evaluationName = evaluationName;
	}

	public void setResults(List<ResultDTO> results) {
		this.results = results;
	}

	
}
