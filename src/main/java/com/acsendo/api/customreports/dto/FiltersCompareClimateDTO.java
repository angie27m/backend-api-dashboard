package com.acsendo.api.customreports.dto;


public class FiltersCompareClimateDTO {

	private Boolean isEnps;

	private Long divisionId;

	private Long subsidiaryId;

	private Long evaluationIdPrimary;

	private Long evaluationIdSecondary;
	
	private String groupSubsidiaries;

	public Boolean getIsEnps() {
		return isEnps;
	}

	public void setIsClimate(Boolean isEnps) {
		this.isEnps = isEnps;
	}

	public Long getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Long divisionId) {
		this.divisionId = divisionId;
	}

	public Long getSubsidiaryId() {
		return subsidiaryId;
	}

	public void setSubsidiaryId(Long subsidiaryId) {
		this.subsidiaryId = subsidiaryId;
	}

	public Long getEvaluationIdPrimary() {
		return evaluationIdPrimary;
	}

	public void setEvaluationIdPrimary(Long evaluationIdPrimary) {
		this.evaluationIdPrimary = evaluationIdPrimary;
	}

	public Long getEvaluationIdSecondary() {
		return evaluationIdSecondary;
	}

	public void setEvaluationIdSecondary(Long evaluationIdSecondary) {
		this.evaluationIdSecondary = evaluationIdSecondary;
	}

	public String getGroupSubsidiaries() {
		if( this.subsidiaryId != null ) 
			this.groupSubsidiaries = String.valueOf(this.subsidiaryId);
		return groupSubsidiaries;
	}

	public void setGroupSubsidiaries(String groupSubsidiaries) {
		this.groupSubsidiaries = groupSubsidiaries;
	}

}
