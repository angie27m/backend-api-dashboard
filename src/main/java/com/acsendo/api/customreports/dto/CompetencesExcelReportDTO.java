package com.acsendo.api.customreports.dto;

import java.math.BigDecimal;

public class CompetencesExcelReportDTO {
	
	private String name;
	
	private String job;
	
	private String division;
	
	private BigDecimal scale;
	
	private BigDecimal percentage;

	public String getName() {
		return name;
	}

	public String getJob() {
		return job;
	}

	public String getDivision() {
		return division;
	}

	public BigDecimal getScale() {
		return scale;
	}

	public BigDecimal getPercentage() {
		return percentage;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public void setScale(BigDecimal scale) {
		this.scale = scale;
	}

	public void setPercentage(BigDecimal percentage) {
		this.percentage = percentage;
	}
	
}
