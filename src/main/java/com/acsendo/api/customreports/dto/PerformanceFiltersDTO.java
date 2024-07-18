package com.acsendo.api.customreports.dto;

/**
 * Filtros para desempeño de empleados
 *
 */
public class PerformanceFiltersDTO {
	

	private String name;
	
	private String typeName;
	
	private String jobCategories;
	
	private String divisions;
	
	private String typeResult;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getJobCategories() {
		return jobCategories;
	}

	public void setJobCategories(String jobCategories) {
		this.jobCategories = jobCategories;
	}

	public String getDivisions() {
		return divisions;
	}

	public void setDivisions(String divisions) {
		this.divisions = divisions;
	}

	public String getTypeResult() {
		return typeResult;
	}

	public void setTypeResult(String typeResult) {
		this.typeResult = typeResult;
	}
	
}
