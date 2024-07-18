package com.acsendo.api.customreports.dto;

import java.util.List;

import com.acsendo.api.customReports.enumerations.PerformanceModule;

public class ModulePerformanceDTO {
	
	private PerformanceModule module;
	
	private String name;

	private List<ProcessDTO> processes;
	

	public PerformanceModule getModule() {
		return module;
	}

	public void setModule(PerformanceModule module) {
		this.module = module;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ProcessDTO> getProcesses() {
		return processes;
	}

	public void setProcesses(List<ProcessDTO> processes) {
		this.processes = processes;
	}
	

}
