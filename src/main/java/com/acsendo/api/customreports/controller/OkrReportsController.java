package com.acsendo.api.customreports.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acsendo.api.customreports.dto.CategoryDTO;
import com.acsendo.api.customreports.dto.FiltersOkrsExcel;
import com.acsendo.api.customreports.dto.PeriodDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.service.OkrReportsService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * Contiene todos los servicios de okr's usados en el reporteador 
 *
 */
@RestController
@RequestMapping(OkrReportsController.MAIN_PATH)
public class OkrReportsController {

	public static final String MAIN_PATH = "customreports/{companyId}/okrs";

	public static final String PERIODS = "/periods";
	
	public static final String PERIOD_ID = PERIODS + "/{periodId}";
	
	public static final String AVG = PERIOD_ID + "/avg";
	
	public static final String OBJECTIVES = PERIOD_ID + "/objectives";
	
	public static final String DIVISIONS = PERIOD_ID +"/divisions";
	
	public static final String YEARS = "/years";
	
	public static final String YEARS_PERIODS = PERIODS +"/avg";

	public static final String RESULTS = PERIOD_ID +"/results";
	
	public static final String EXCEL_REPORT = PERIOD_ID + "/report";
	
	public static final String EMPLOYEE=PERIOD_ID+"/employees/{employeeId}";
	
	@Autowired
	private OkrReportsService okrReportsService;
	
	
	@ApiOperation(value = "Obtiene listado de los períodos de okrs de una empresa", response = PeriodDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(PERIODS)
	public List<PeriodDTO> getOkrPeriodsByCompany(@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "isLeader", value = "Indica si es colaborador o líder", required = false) @RequestParam(required = false) Boolean isLeader) {		
		List<PeriodDTO> list = okrReportsService.getOkrPeriodsByCompany(companyId, employeeId, isLeader);
		return list;
	}	
		
	@ApiOperation(value = "Obtiene el promedio general de un período de OKRS (Puede recibir ids de departamento)")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(AVG)
	public Double getAvgOkrsCompany(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento(s)", required = false) @RequestParam(required = false) String divisionId) {
		try {
			 return okrReportsService.getAvgOkrsCompany(periodId, divisionId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	

	@ApiOperation(value = "Metodo que obtiene el promedio de resultados por objetivos de un período de okrs")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(OBJECTIVES)
	public List<CategoryDTO> getAvgOkrsGroupByObjective(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de okrs", required = true) @PathVariable Long periodId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento(s)", required = false) @RequestParam(required = false) String divisionId){
		try {
			  return okrReportsService.getAvgOkrsGroupByObjective(periodId, divisionId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@ApiOperation(value = "Metodo que obtiene el promedio de okrs agrupado por departamentos")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(DIVISIONS)
	public List<ResultDTO> getAvgOkrsGroupByDivision(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId) {
		try {
			  return okrReportsService.getAvgOkrsGroupByDivision(periodId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	

	@ApiOperation(value = "Metodo que obtiene los años junto con su promedio")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(YEARS)
	public List<ResultDTO> getAvgOkrsByYears(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId) {
		try {
			  return okrReportsService.getAvgOkrsByYears(companyId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	@ApiOperation(value = "Metodo que obtiene los períodos de un año junto con su promedio")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(YEARS_PERIODS)
	public List<ResultDTO> getPeriodsOkrsByYear(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "year", value = "Identificador del año a consultar", required = true) @RequestParam (required = true) Long year) {
		try {
			  return okrReportsService.getPeriodsOkrsByYear(companyId, year);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
		
	}

	@ApiOperation(value = "Metodo que obtiene el promedio de resultados por key results o iniciativas en un período de okrs de un empleado")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(RESULTS)
	public List<ResultDTO> getOkrResultsByEmployee(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "showKR", value = "Indica si se muestran key results o iniciativas (true para KR, false para iniciativas)", required = true) @RequestParam(required = true) boolean showKR,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = true) @RequestParam(required = true) Long employeeId) {
		try {
			  return okrReportsService.getOkrResultsByEmployee(periodId, employeeId, showKR);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@ApiOperation(value = "Obtiene reporte de excel de OKRS")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(EXCEL_REPORT)
	public ResponseEntity<byte[]> getCompetencesReport(
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = false) @PathVariable Long companyId,
			@RequestBody FiltersOkrsExcel filters){
		try {
			byte[] excelReport =  okrReportsService.getOKRSExcelReport(companyId, periodId, filters);			
			return new ResponseEntity<byte[]>(excelReport, HttpStatus.OK);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Metodo que obtiene los objetivos con los resultados claves asignados a un empleado")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EMPLOYEE)
	public List<CategoryDTO> getOkrResultsAndObjectivesByEmployee(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = true) @PathVariable Long employeeId) {
		try {
			  return okrReportsService.getKeyResultsByEmployee(periodId, employeeId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
}
