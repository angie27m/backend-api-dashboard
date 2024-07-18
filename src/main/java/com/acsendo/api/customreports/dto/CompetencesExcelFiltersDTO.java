package com.acsendo.api.customreports.dto;

public class CompetencesExcelFiltersDTO {
	
	private Long mainEvaluationId;
	
	private Long ComparedEvaluationId;
	
	private Long firstDivisionId;
	
	private Long secondDivisionId;
	
	private Long thirdDivisionId;
	
	private Long employeeCompetencesDivisionId;
	
	private Long firstSubsidiaryId;
	
	private Long secondSubsidiaryId;
	
	private Long thirdSubsidiaryId;
	
	private Long divisionIdJobFilter;
	
	private Long competenceId;
	
	public Long getDivisionIdJobFilter() {
		return divisionIdJobFilter;
	}

	public Long getCompetenceId() {
		return competenceId;
	}

	public void setDivisionIdJobFilter(Long divisionIdJobFilter) {
		this.divisionIdJobFilter = divisionIdJobFilter;
	}

	public void setCompetenceId(Long competenceId) {
		this.competenceId = competenceId;
	}

	private boolean ByCategory;
	
	private boolean isFiltered;

	public boolean isFiltered() {
		return isFiltered;
	}

	public void setIsFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}

	public Long getMainEvaluationId() {
		return mainEvaluationId;
	}

	public Long getComparedEvaluationId() {
		return ComparedEvaluationId;
	}

	public Long getFirstDivisionId() {
		return firstDivisionId;
	}

	public Long getSecondDivisionId() {
		return secondDivisionId;
	}

	public Long getThirdDivisionId() {
		return thirdDivisionId;
	}

	public Long getEmployeeCompetencesDivisionId() {
		return employeeCompetencesDivisionId;
	}
	
	public Long getFirstSubsidiaryId() {
		return firstSubsidiaryId;
	}

	public Long getSecondSubsidiaryId() {
		return secondSubsidiaryId;
	}

	public Long getThirdSubsidiaryId() {
		return thirdSubsidiaryId;
	}

	public void setFirstSubsidiaryId(Long firstSubsidiaryId) {
		this.firstSubsidiaryId = firstSubsidiaryId;
	}

	public void setSecondSubsidiaryId(Long secondSubsidiaryId) {
		this.secondSubsidiaryId = secondSubsidiaryId;
	}

	public void setThirdSubsidiaryId(Long thirdSubsidiaryId) {
		this.thirdSubsidiaryId = thirdSubsidiaryId;
	}

	public boolean isByCategory() {
		return ByCategory;
	}

	public void setMainEvaluationId(Long mainEvaluationId) {
		this.mainEvaluationId = mainEvaluationId;
	}

	public void setComparedEvaluationId(Long comparedEvaluationId) {
		ComparedEvaluationId = comparedEvaluationId;
	}

	public void setFirstDivisionId(Long firstDivisionId) {
		this.firstDivisionId = firstDivisionId;
	}

	public void setSecondDivisionId(Long secondDivisionId) {
		this.secondDivisionId = secondDivisionId;
	}

	public void setThirdDivisionId(Long thirdDivisionId) {
		this.thirdDivisionId = thirdDivisionId;
	}

	public void setEmployeeCompetencesDivisionId(Long employeeCompetencesDivisionID) {
		this.employeeCompetencesDivisionId = employeeCompetencesDivisionID;
	}
	
	public void setByCategory(boolean byCategory) {
		ByCategory = byCategory;
	}
	
	
}
