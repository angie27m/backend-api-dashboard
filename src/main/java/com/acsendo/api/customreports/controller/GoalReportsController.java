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
import com.acsendo.api.customreports.dto.FilterDTO;
import com.acsendo.api.customreports.dto.FiltersResultsDTO;
import com.acsendo.api.customreports.dto.PeriodDTO;
import com.acsendo.api.customreports.dto.ResponseDTO;
import com.acsendo.api.customreports.dto.GoalTrackDTO;
import com.acsendo.api.customreports.dto.GoalsExcelFiltersDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.dto.SemaphoreDTO;
import com.acsendo.api.customreports.dto.StrategicGoalDTO;
import com.acsendo.api.customreports.service.GoalReportsService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * Contiene todos los servicios de metas usados en el reporteador 
 *
 */
@RestController
@RequestMapping(GoalReportsController.MAIN_PATH)
public class GoalReportsController {

	public static final String MAIN_PATH = "customreports/{companyId}/goals";

	public static final String PERIODS = "/periods";
	
	public static final String PERIOD_ID = PERIODS + "/{periodId}";
	
	public static final String FILTERS = "/filters";
	
	public static final String SEMAPHORE = PERIOD_ID + "/semaphore";
	
	public static final String AVG = PERIOD_ID + "/avg";

	public static final String STRATEGIC = PERIOD_ID + "/strategic";
	
	public static final String STRATEGIC_ID = STRATEGIC + "/{strategicId}";

	public static final String LEVELS = PERIOD_ID + "/levels";
	
	public static final String DIVISIONS = PERIOD_ID +"/divisions";
	
	public static final String TRACKS = STRATEGIC_ID + "/tracks";
	
	public static final String EXCEL_REPORT = PERIOD_ID + "/report";
	
	public static final String CALL_PROCEDURE= PERIOD_ID + "/procedure";
	
	public static final String EMPLOYEES = PERIOD_ID + "/employees";
	
	public static final String TEMPLATES = PERIOD_ID + "/templates";
	
	@Autowired
	private GoalReportsService goalReportsService;
		
	
	@ApiOperation(value = "Obtiene listado de los períodos de metas de una empresa", response = PeriodDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(PERIODS)
	public List<PeriodDTO> getGoalsByCompany(@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "isLeader", value = "Indica si es colaborador o líder", required = false) @RequestParam(required = false) Boolean isLeader,
			@ApiParam(name = "resultsDashboardState", value = "Verifica si necesita los periodos para el dashboard de estado de metas", required = false) @RequestParam(required = false) Boolean resultsDashboardState ) {		
		List<PeriodDTO> list = goalReportsService.getGoalsByCompany(companyId, employeeId, isLeader, resultsDashboardState);
		return list;
	}	
	
	@ApiOperation(value = "Método que consulta los departamentos, sedes y/o  niveles de cargos que tienen resultados de metas")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(FILTERS)
	public List<FilterDTO> getFiltersGoals(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del periodo de metas", required = true) @RequestParam Long periodId,
			@ApiParam(name = "typeFilter", value = "Tipo de Filtro: DIVISION,SUBSIDIARY o LEVEL", required = true) @RequestParam String typeFilter,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin
			) {
		try {
			return goalReportsService.getFiltersGoals(companyId, periodId, typeFilter, subsidiariesAdmin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Metodo que permite obtener los limites, colores y las etiquetas del semáforo de un período de metas")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SEMAPHORE)
	public List<SemaphoreDTO> getEvaluationSemaphore(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId) {
		try {
			  return goalReportsService.getSemaphore(periodId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@ApiOperation(value = "Obtiene el promedio general de un período de metas (Puede recibir filtros)")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(AVG)
	public Double getAvgGoalsResults(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "isLeader", value = "Indica si es colaborador o líder", required = false) @RequestParam(required = false) Boolean isLeader,
			@RequestBody FiltersResultsDTO filters) {
		try {
			 return goalReportsService.getAvgGoalsCompany(periodId, filters, employeeId, isLeader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	

	
	@ApiOperation(value = "Obtiene el promedio de metas agrupado por nivel de cargo y departamentos (Puede recibir filtros)")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(LEVELS)
	public List<CategoryDTO> getAvgGroupByLevelsAndDivision(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período", required = true) @PathVariable Long periodId,
			@RequestBody FiltersResultsDTO filters) {
		try {
			 return goalReportsService.getAvgGoalGroupByLevelAndDivision(periodId, filters);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	

	@ApiOperation(value = "Metodo que obtiene el promedio de resultados por metas estratégicas de un período")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(STRATEGIC)
	public List<ResultDTO> getAvgStrategicGoals(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			  return goalReportsService.getAvgStrategicGoals(periodId, subsidiariesAdmin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@ApiOperation(value = "Metodo que obtiene el promedio de metas agrupado por departamentos")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(DIVISIONS)
	public List<ResultDTO> getAvgGoalGroupByDivision(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			  return goalReportsService.getAvgGoalsGroupByDivision(periodId, subsidiariesAdmin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@ApiOperation(value = "Metodo que obtiene la cantidad de seguimientos realizados de una meta estratégica")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(STRATEGIC_ID)
	public StrategicGoalDTO getTracksByStrategicGoal(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "strategicId", value = "Identificador de la meta estratégica", required = true) @PathVariable Long strategicId,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			  return goalReportsService.getTracksByStrategicGoal(companyId, periodId, strategicId, subsidiariesAdmin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Metodo que obtiene el cumplimiento de seguimientos realizados de una meta estratégica de acuerdo a un período de seguimiento")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(TRACKS)
	public List<GoalTrackDTO> getAverageTracksByStrategicGoal(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "strategicId", value = "Identificador de la meta estratégica", required = true) @PathVariable Long strategicId,
			@ApiParam(name = "showLeader", value = "Mostrar resultados del líder", required = true)  @RequestParam(required = true) boolean showLeader,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false)  @RequestParam(required = false) Long divisionId,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			  return goalReportsService.getAverageTracksByStrategicGoal(periodId, strategicId, divisionId, showLeader, subsidiariesAdmin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene reporte de excel de Metas")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(EXCEL_REPORT)
	public ResponseEntity<byte[]> getGoalsExcelReport(
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = false) @PathVariable Long companyId,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin,
			@RequestBody GoalsExcelFiltersDTO filters){
		try {
			byte[] excelReport =  goalReportsService.getGoalsExcelReport(companyId, periodId, filters, subsidiariesAdmin);			
			return new ResponseEntity<byte[]>(excelReport, HttpStatus.OK);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	} 
	
	@ApiOperation(value = "Llama al procedimiento almacenado que recalcula la data de metas")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(CALL_PROCEDURE)
	public void callStoredProcedure(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId) {
		try {
			goalReportsService.executeStoredProcedureGoals(companyId, periodId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Metodo que obtiene el listado de metas de un empleado con su promedio y peso")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EMPLOYEES)
	public List<ResponseDTO> getGoalsListByEmployee(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = true) @RequestParam(required = true) Long employeeId,
			@ApiParam(name = "isLeader", value = "Indica si es colaborador o líder", required = true) @RequestParam(required = true) Boolean isLeader) {
		try {
			  return goalReportsService.getGoalsListByEmployee(periodId, employeeId, isLeader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@ApiOperation(value = "Obtiene id de plantilla de reporte de metas")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(TEMPLATES)
	public Long getPdfReportTemplate(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = true) @PathVariable Long periodId) {
		try {
			return goalReportsService.getPdfReportTemplate(companyId, periodId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
