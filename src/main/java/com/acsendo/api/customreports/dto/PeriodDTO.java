package com.acsendo.api.customreports.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PeriodDTO {
	
	
	private Long id;
	private String name;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private Date startDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private Date endDate;
	
	private List<SemaphoreDTO> semaphore;
	
	private Long goalsLimit;
	
    public PeriodDTO() {
        super();
    }
	
	public PeriodDTO(Long id, String name, Date startdate, Date enddate) {
		super();
		this.id = id;
		this.name = name;
		this.startDate = startdate;
		this.endDate = enddate;
	}
	
	public PeriodDTO(Long id, String name, Date startdate, Date enddate, Long goalsLimit) {
		super();
		this.id = id;
		this.name = name;
		this.startDate = startdate;
		this.endDate = enddate;
		this.goalsLimit = goalsLimit;
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
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
		
	public List<SemaphoreDTO> getSemaphore() {
		return semaphore;
	}

	public void setSemaphore(List<SemaphoreDTO> semaphore) {
		this.semaphore = semaphore;
	}

	public Long getGoalsLimit() {
		return goalsLimit;
	}

	public void setGoalsLimit(Long goalsLimit) {
		this.goalsLimit = goalsLimit;
	}
	
}
