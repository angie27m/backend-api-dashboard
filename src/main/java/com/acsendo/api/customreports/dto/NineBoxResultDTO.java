package com.acsendo.api.customreports.dto;

import java.util.List;

public class NineBoxResultDTO {
	
	
	private Long id;
	private String name;
	private String photo;
	private List<Double> dataArray ;
	private List<String> dataDesc;
	
	
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
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public List<Double> getDataArray() {
		return dataArray;
	}
	public void setDataArray(List<Double> dataArray) {
		this.dataArray = dataArray;
	}
	public List<String> getDataDesc() {
		return dataDesc;
	}
	public void setDataDesc(List<String> dataDesc) {
		this.dataDesc = dataDesc;
	}


}
