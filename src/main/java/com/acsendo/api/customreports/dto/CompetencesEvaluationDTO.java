package com.acsendo.api.customreports.dto;

import java.util.Date;
import java.util.List;

import com.acsendo.api.hcm.enumerations.EvaluationState2;
import com.fasterxml.jackson.annotation.JsonFormat;

public class CompetencesEvaluationDTO {
	
	private Long id;
	
	private String name;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date startDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date finalDate;
	
	private EvaluationState2 state;
	
	private Double responseRate;
	
	private Long competenceModelId;
	
	private List<SemaphoreDTO> semaphore;
	
	private Double average;
	
	private Double compLimit;
	
	private String typeCalibration;
	
	private String stateCalibrationEvaluation;
	
	private Boolean viewCalibration;
	
	private boolean showSwitchResultsCalibration;
	
	private Boolean hideRelations;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getFinalDate() {
		return finalDate;
	}

	public void setFinalDate(Date finalDate) {
		this.finalDate = finalDate;
	}

	public EvaluationState2 getState() {
		return state;
	}

	public void setState(EvaluationState2 state) {
		this.state = state;
	}

	public Double getResponseRate() {
		return responseRate;
	}

	public void setResponseRate(Double responseRate) {
		this.responseRate = responseRate;
	}

	public Long getCompetenceModelId() {
		return competenceModelId;
	}

	public void setCompetenceModelId(Long competenceModelId) {
		this.competenceModelId = competenceModelId;
	}

	public List<SemaphoreDTO> getSemaphore() {
		return semaphore;
	}

	public void setSemaphore(List<SemaphoreDTO> semaphore) {
		this.semaphore = semaphore;
	}

	public Double getAverage() {
		return average;
	}

	public void setAverage(Double average) {
		this.average = average;
	}

	public Double getCompLimit() {
		return compLimit;
	}

	public void setCompLimit(Double compLimit) {
		this.compLimit = compLimit;
	}

	public String getTypeCalibration() {
		return typeCalibration;
	}

	public void setTypeCalibration(String typeCalibration) {
		this.typeCalibration = typeCalibration;
	}

	public String getStateCalibrationEvaluation() {
		return stateCalibrationEvaluation;
	}

	public void setStateCalibrationEvaluation(String stateCalibrationEvaluation) {
		this.stateCalibrationEvaluation = stateCalibrationEvaluation;
	}

	public Boolean getViewCalibration() {
		return viewCalibration;
	}

	public void setViewCalibration(Boolean viewCalibration) {
		this.viewCalibration = viewCalibration;
	}

	public boolean isShowSwitchResultsCalibration() {
		return showSwitchResultsCalibration;
	}

	public void setShowSwitchResultsCalibration(boolean showSwitchResultsCalibration) {
		this.showSwitchResultsCalibration = showSwitchResultsCalibration;
	}

	public Boolean isHideRelations() {
		return hideRelations;
	}

	public void setHideRelations(Boolean hideRelations) {
		this.hideRelations = hideRelations;
	}
	


}
