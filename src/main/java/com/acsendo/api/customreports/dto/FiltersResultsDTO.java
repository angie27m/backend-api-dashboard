package com.acsendo.api.customreports.dto;

import java.util.List;

import com.acsendo.api.extrafield.dto.ExtraFieldValueFilterDTO;

public class FiltersResultsDTO {
	
	private String divisionsId;
	private Long subsidiaryId;
	private Long levelId;
	private String jobName;
	private String subsidiaries;
	private Long periodId;
	private Long evaluationId;
	private Boolean isOkrs;
	private String subsidiariesAdmin;
	private Long divisionId;
	private Long employeeId;
	private Boolean isLeader;
	private List<ExtraFieldValueFilterDTO> extraField;
	private Boolean calibrated;
	
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
	
	
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getSubsidiaries() {
		return subsidiaries;
	}
	public void setSubsidiaries(String subsidiaries) {
		this.subsidiaries = subsidiaries;
	}
	public Long getPeriodId() {
		return periodId;
	}
	public void setPeriodId(Long periodId) {
		this.periodId = periodId;
	}
	public Long getEvaluationId() {
		return evaluationId;
	}
	public void setEvaluationId(Long evaluationId) {
		this.evaluationId = evaluationId;
	}
	public Boolean getIsOkrs() {
		return isOkrs;
	}
	public void setIsOkrs(Boolean isOkrs) {
		this.isOkrs = isOkrs;
	}
	public String getSubsidiariesAdmin() {
		return subsidiariesAdmin;
	}
	public void setSubsidiariesAdmin(String subsidiariesAdmin) {
		this.subsidiariesAdmin = subsidiariesAdmin;
	}
	public Long getDivisionId() {
		return divisionId;
	}
	public void setDivisionId(Long divisionId) {
		this.divisionId = divisionId;
	}
	public Long getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}
	public Boolean getIsLeader() {
		return isLeader;
	}
	public void setIsLeader(Boolean isLeader) {
		this.isLeader = isLeader;
	}
	public List<ExtraFieldValueFilterDTO> getExtraField() {
		return extraField;
	}
	public void setExtraField(List<ExtraFieldValueFilterDTO> extraField) {
		this.extraField = extraField;
	}
	public Boolean getCalibrated() {
		return calibrated;
	}
	public void setCalibrated(Boolean calibrated) {
		this.calibrated = calibrated;
	}
	

}
