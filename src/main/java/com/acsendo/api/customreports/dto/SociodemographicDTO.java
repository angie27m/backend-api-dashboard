package com.acsendo.api.customreports.dto;

import java.util.List;

public class SociodemographicDTO {
	
	private Long id;

	private String name;

	private List<ResponseDTO> results;
	
	
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

	public List<ResponseDTO> getResults() {
		return results;
	}

	public void setResults(List<ResponseDTO> results) {
		this.results = results;
	}

}
