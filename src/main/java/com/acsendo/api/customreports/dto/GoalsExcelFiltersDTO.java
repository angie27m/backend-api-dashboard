package com.acsendo.api.customreports.dto;

public class GoalsExcelFiltersDTO {

	private String divisionsId;

	private Long subsidiaryId;

	private Long levelId;

	private Long strategicGoalId;

	private Boolean showLeader;

	private Long divisionIdGoal;
	

	public String getDivisionsId() {
		return divisionsId;
	}

	public void setDivisionsId(String divisionsId) {
		this.divisionsId = divisionsId;
	}

	public Long getSubsidiaryId() {
		return subsidiaryId;
	}

	public void setSubsidiaryId(Long subsidiaryId) {
		this.subsidiaryId = subsidiaryId;
	}

	public Long getLevelId() {
		return levelId;
	}

	public void setLevelId(Long levelId) {
		this.levelId = levelId;
	}

	public Long getStrategicGoalId() {
		return strategicGoalId;
	}

	public void setStrategicGoalId(Long strategicGoalId) {
		this.strategicGoalId = strategicGoalId;
	}

	public Boolean getShowLeader() {
		return showLeader;
	}

	public void setShowLeader(Boolean showLeader) {
		this.showLeader = showLeader;
	}

	public Long getDivisionIdGoal() {
		return divisionIdGoal;
	}

	public void setDivisionIdGoal(Long divisionIdGoal) {
		this.divisionIdGoal = divisionIdGoal;
	}

}
