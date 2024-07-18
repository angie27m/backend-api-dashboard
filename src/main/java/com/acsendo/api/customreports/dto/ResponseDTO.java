package com.acsendo.api.customreports.dto;

public class ResponseDTO {

	private Long id;
	private String label;
	private Double percentage;
	private Integer countResponses;
	private Double weight;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	public Integer getCountResponses() {
		return countResponses;
	}

	public void setCountResponses(Integer countResponses) {
		this.countResponses = countResponses;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

}
