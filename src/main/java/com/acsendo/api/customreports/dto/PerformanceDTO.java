package com.acsendo.api.customreports.dto;

import java.util.List;

public class PerformanceDTO {

	private Integer weightCompetences;
	private Integer weightGoalsOkrs;
	private List<SemaphoreDTO> semaphore;
	private String competencesFormat;
	private String goalsFormat;
	private Double limit;
	private Long evaluationSelectedId;
	private Long goalPeriodId;
	

	public Integer getWeightCompetences() {
		return weightCompetences;
	}

	public void setWeightCompetences(Integer weightCompetences) {
		this.weightCompetences = weightCompetences;
	}

	public Integer getWeightGoalsOkrs() {
		return weightGoalsOkrs;
	}

	public void setWeightGoalsOkrs(Integer weightGoalsOkrs) {
		this.weightGoalsOkrs = weightGoalsOkrs;
	}

	public List<SemaphoreDTO> getSemaphore() {
		return semaphore;
	}

	public void setSemaphore(List<SemaphoreDTO> semaphore) {
		this.semaphore = semaphore;
	}

	public String getCompetencesFormat() {
		return competencesFormat;
	}

	public void setCompetencesFormat(String competencesFormat) {
		this.competencesFormat = competencesFormat;
	}

	public String getGoalsFormat() {
		return goalsFormat;
	}

	public void setGoalsFormat(String goalsFormat) {
		this.goalsFormat = goalsFormat;
	}

	public Double getLimit() {
		return limit;
	}

	public void setLimit(Double limit) {
		this.limit = limit;
	}

	public Long getEvaluationSelectedId() {
		return evaluationSelectedId;
	}

	public void setEvaluationSelectedId(Long evaluationSelectedId) {
		this.evaluationSelectedId = evaluationSelectedId;
	}

	public Long getGoalPeriodId() {
		return goalPeriodId;
	}

	public void setGoalPeriodId(Long goalPeriodId) {
		this.goalPeriodId = goalPeriodId;
	}
	

}
