package com.acsendo.api.customreports.dto;


public class KeywordClimateDTO {

	private String word;
	
	private Double percentage;

	private String type;
	
	private Double positivePercentage;
	
	private Double neutralPercentage;
	
	private Double mixedPercentage;
	
	private Double negativePercentage;
	
	private int totalCount;
	

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getPositivePercentage() {
		return positivePercentage;
	}

	public void setPositivePercentage(Double positivePercentage) {
		this.positivePercentage = positivePercentage;
	}

	public Double getNeutralPercentage() {
		return neutralPercentage;
	}

	public void setNeutralPercentage(Double neutralPercentage) {
		this.neutralPercentage = neutralPercentage;
	}

	public Double getMixedPercentage() {
		return mixedPercentage;
	}

	public void setMixedPercentage(Double mixedPercentage) {
		this.mixedPercentage = mixedPercentage;
	}

	public Double getNegativePercentage() {
		return negativePercentage;
	}

	public void setNegativePercentage(Double negativePercentage) {
		this.negativePercentage = negativePercentage;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
	
}
