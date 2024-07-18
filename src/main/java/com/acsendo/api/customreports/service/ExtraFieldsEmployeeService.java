package com.acsendo.api.customreports.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acsendo.api.customreports.dto.FiltersResultsDTO;
import com.acsendo.api.extrafield.dto.ExtraFieldValueFilterDTO;
import com.acsendo.api.extrafield.repository.ExtraFieldRepository;
import com.acsendo.api.hcm.repository.EmployeeRepository;


@Service
public class ExtraFieldsEmployeeService {
	
	
	@Autowired
	private ExtraFieldRepository extraFieldRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	
	public void validateLevelAndJobFilters(FiltersResultsDTO filters) {
		
		if(filters.getExtraField()!=null && filters.getExtraField().size()>0) {
			Optional<ExtraFieldValueFilterDTO> level=filters.getExtraField().stream().filter(l-> l.getType().equals("LEVEL")).findFirst();
			if(level.isPresent()) {
				filters.setLevelId(level.get().getExtraFieldId());
			}
			
			Optional<ExtraFieldValueFilterDTO> job=filters.getExtraField().stream().filter(l-> l.getType().equals("JOB")).findFirst();
			if(job.isPresent()) {
				filters.setJobName(job.get().getValue());
			}
			
		}
	}
	
	public Map <Long, Long> validateExtraFieldsEmployeesFilter(FiltersResultsDTO filters, Long companyId) {
		
		Map <Long, Long> employeesIdsWithExtraFields= null;
		
		if(filters.getExtraField()!=null) {
			 
			String employees=null;
			if(filters.getIsLeader()!=null && filters.getIsLeader()) {
			  Optional<List<Long>> collOpt=employeeRepository.findCollaboratorsIdByBoss(filters.getEmployeeId());

			  if(collOpt.isPresent()) {
				  List<Long>  collaboratorIds =  collOpt.get();
				employees=StringUtils.join(collaboratorIds, ',');
			  }
			}
			
			String extraFieldsIds=filters.getExtraField().stream().filter(ext-> ext.getType().equals("ExtraField")).map(ext-> ext.getExtraFieldId().toString()).collect(Collectors.joining(", "));
		    String extraFieldsValuesIds=filters.getExtraField().stream().filter(ext-> ext.getType().equals("ExtraField")).map(ext-> ext.getValue()).collect(Collectors.joining("', '"));
				
			if(!extraFieldsIds.isEmpty() && !extraFieldsValuesIds.isEmpty()) {
			      List<BigInteger> idsEmployeesWithExtraFields=extraFieldRepository.getExtraFieldsValuesByEmployeesAndFilters(employees, extraFieldsIds, extraFieldsValuesIds, companyId);
			      if(idsEmployeesWithExtraFields!=null) {
			      employeesIdsWithExtraFields = idsEmployeesWithExtraFields.stream().collect(Collectors.toMap(BigInteger::longValue, item -> item.longValue()));
			
			      }
			}
		}
		
		return employeesIdsWithExtraFields;
	}

}
