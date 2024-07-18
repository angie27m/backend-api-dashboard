package com.acsendo.api.customreports.dto;

public class FiltersOkrsExcel {
	
	private String year;
	private Long divisionId;
	private Long employeeDivisionId;
	private Long employeeId;
	private boolean isByCompany;
	private boolean byKr;
	private boolean isFiltered;
	
	public String getYear() {
		return year;
	}
	public Long getDivisionId() {
		return divisionId;
	}
	public Long getEmployeeDivisionId() {
		return employeeDivisionId;
	}
	public Long getEmployeeId() {
		return employeeId;
	}
	public boolean getIsByCompany() {
		return isByCompany;
	}
	public boolean getByKr() {
		return byKr;
	}
	public boolean getIsFiltered() {
		return isFiltered;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public void setDivisionId(Long divisionId) {
		this.divisionId = divisionId;
	}
	public void setEmployeeDivisionId(Long employeeDivisionId) {
		this.employeeDivisionId = employeeDivisionId;
	}
	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}
	public void setIsByCompany(boolean isByCompany) {
		this.isByCompany = isByCompany;
	}
	public void setByKr(boolean byKr) {
		this.byKr = byKr;
	}
	public void setIsFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}
	
	
	
}
