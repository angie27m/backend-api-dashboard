package com.acsendo.api.customreports.dto;


public class FiltersClimateDTO {
	
	
	private Boolean isEnps;
	
	private Long divisionId;
	
	private Long subsidiaryId;
	
	private Boolean isByDimension;
	
	private String groupSubsidiaries;
	
	/**
	 *  Identificador del factor o de la dimensión
	 **/
	private Long entityId;
	
	
	public Boolean getIsEnps() {
		return isEnps;
	}
	
	public void setIsEnps(Boolean isEnps) {
		this.isEnps = isEnps;
	}
	
	public void setIsClimate(Boolean isEnps) {
		this.isEnps = isEnps;
	}

	public Long getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Long divisionId) {
		this.divisionId = divisionId;
	}

	public Long getSubsidiaryId() {
		return subsidiaryId;
	}

	public void setSubsidiaryId(Long subsidiaryId) {
		this.subsidiaryId = subsidiaryId;
	}

	public Boolean getIsByDimension() {
		return isByDimension;
	}

	public void setIsByDimension(Boolean isByDimension) {
		this.isByDimension = isByDimension;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public String getGroupSubsidiaries() {
		if( this.subsidiaryId != null ) 
			this.groupSubsidiaries = String.valueOf(this.subsidiaryId);
		return groupSubsidiaries;
	}

	public void setGroupSubsidiaries(String groupSubsidiaries) {
		this.groupSubsidiaries = groupSubsidiaries;
	}

}
