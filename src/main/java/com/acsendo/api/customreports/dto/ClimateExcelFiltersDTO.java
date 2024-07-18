package com.acsendo.api.customreports.dto;

/**
 * Contiene los filtros por aplicar al reporte excel de clima
 * 
 * @author Angie Manrique
 *
 */
public class ClimateExcelFiltersDTO {
	
	private Boolean isByDimension;

	private Long subsidiaryIdHeatMap;

	private Long subsidiaryIdQuestion;

	private Long divisionIdQuestion;

	private String wordOpen;

	private Long questionId;

	private Long divisionIdOpen;
	

	public Boolean getIsByDimension() {
		return isByDimension;
	}

	public void setIsByDimension(Boolean isByDimension) {
		this.isByDimension = isByDimension;
	}

	public Long getSubsidiaryIdHeatMap() {
		return subsidiaryIdHeatMap;
	}

	public void setSubsidiaryIdHeatMap(Long subsidiaryIdHeatMap) {
		this.subsidiaryIdHeatMap = subsidiaryIdHeatMap;
	}

	public Long getSubsidiaryIdQuestion() {
		return subsidiaryIdQuestion;
	}

	public void setSubsidiaryIdQuestion(Long subsidiaryIdQuestion) {
		this.subsidiaryIdQuestion = subsidiaryIdQuestion;
	}

	public Long getDivisionIdQuestion() {
		return divisionIdQuestion;
	}

	public void setDivisionIdQuestion(Long divisionIdQuestion) {
		this.divisionIdQuestion = divisionIdQuestion;
	}

	public String getWordOpen() {
		return wordOpen;
	}

	public void setWordOpen(String wordOpen) {
		this.wordOpen = wordOpen;
	}

	public Long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public Long getDivisionIdOpen() {
		return divisionIdOpen;
	}

	public void setDivisionIdOpen(Long divisionIdOpen) {
		this.divisionIdOpen = divisionIdOpen;
	}

}
