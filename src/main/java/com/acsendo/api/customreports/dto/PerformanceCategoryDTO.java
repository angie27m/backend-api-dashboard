package com.acsendo.api.customreports.dto;


public class PerformanceCategoryDTO {
	
	private Long id;
	
	private Long companyLevelId;
	
	private Double weight;
	
	
	public PerformanceCategoryDTO() {
		super();
	}

	public PerformanceCategoryDTO(Long id, Long companyLevelId, Double weight) {
		super();
		this.id = id;
		this.companyLevelId = companyLevelId;
		this.weight = weight;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getCompanyLevelId() {
		return companyLevelId;
	}

	public void setCompanyLevelId(Long companyLevelId) {
		this.companyLevelId = companyLevelId;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}	
	

}
