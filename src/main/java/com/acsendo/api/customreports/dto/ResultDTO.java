package com.acsendo.api.customreports.dto;

public class ResultDTO {
	
	private Long id;

	private String name;
	
	private Double value;
	
	
	public ResultDTO() {
		super();
	}

	public ResultDTO(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	

	public ResultDTO(Long id, String name, Double value) {
		super();
		this.id = id;
		this.name = name;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
	
	
}
