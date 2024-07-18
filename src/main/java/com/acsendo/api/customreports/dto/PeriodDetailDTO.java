package com.acsendo.api.customreports.dto;

import java.util.List;

public class PeriodDetailDTO {
	
	
	private PeriodDTO period;
	
	private List<ModulePerformanceDTO> modules;
	

	public PeriodDTO getPeriod() {
		return period;
	}

	public void setPeriod(PeriodDTO period) {
		this.period = period;
	}

	public List<ModulePerformanceDTO> getModules() {
		return modules;
	}

	public void setModules(List<ModulePerformanceDTO> modules) {
		this.modules = modules;
	}
	
	
}
