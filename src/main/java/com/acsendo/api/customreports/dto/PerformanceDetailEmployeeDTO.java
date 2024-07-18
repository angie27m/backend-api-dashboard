package com.acsendo.api.customreports.dto;

import java.util.List;

import com.acsendo.api.customReports.dto.PerformanceProcessEmployeeDTO;

public class PerformanceDetailEmployeeDTO {
	
	
	private Double average;
	
	private String type;
	
	private List<PerformanceProcessEmployeeDTO> processes;

	public Double getAverage() {
		return average;
	}

	public void setAverage(Double average) {
		this.average = average;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<PerformanceProcessEmployeeDTO> getProcesses() {
		return processes;
	}

	public void setProcesses(List<PerformanceProcessEmployeeDTO> processes) {
		this.processes = processes;
	}
	
}



