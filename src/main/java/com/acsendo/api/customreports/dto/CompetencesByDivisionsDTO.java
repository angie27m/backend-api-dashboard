package com.acsendo.api.customreports.dto;

import java.util.List;

public class CompetencesByDivisionsDTO {

	private Long divisionId;

	private String divisionName;
	
	private List<ResultDTO> competences;

	private List<CategoryDTO> results;
	

	public Long getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Long divisionId) {
		this.divisionId = divisionId;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	public List<CategoryDTO> getResults() {
		return results;
	}

	public void setResults(List<CategoryDTO> results) {
		this.results = results;
	}

	public List<ResultDTO> getCompetences() {
		return competences;
	}

	public void setCompetences(List<ResultDTO> competences) {
		this.competences = competences;
	}
	
	

}
