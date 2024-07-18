package com.acsendo.api.customreports.dto;

import java.util.List;

public class EmployeeResultExcelDTO {
	
	private Long employeeId;

	private String employeeName;
	
	private Long divisionId;

	private String divisionName;
	
	private Long jobId;

	private String jobName;
	
	private Double value;
	
	private List<CategoryDTO> results;
	

	public List<CategoryDTO> getResults() {
		return results;
	}

	public void setResults(List<CategoryDTO> results) {
		this.results = results;
	}

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

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

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}	
	
}
