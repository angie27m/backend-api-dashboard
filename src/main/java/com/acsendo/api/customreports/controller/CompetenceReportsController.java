package com.acsendo.api.customreports.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.customreports.dto.BehaviorDetailDTO;
import com.acsendo.api.customreports.dto.CategoryDTO;
import com.acsendo.api.customreports.dto.CompetencesByDivisionsDTO;
import com.acsendo.api.customreports.dto.CompetencesEvaluationDTO;
import com.acsendo.api.customreports.dto.CompetencesExcelFiltersDTO;
import com.acsendo.api.customreports.dto.FiltersResultsDTO;
import com.acsendo.api.customreports.dto.QuestionCommentDTO;
import com.acsendo.api.customreports.dto.ResponseDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.dto.SemaphoreDTO;
import com.acsendo.api.customreports.service.CompetenceReportsService;
import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.recruiting.dto.RecruitingPostulationStageDTO;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * Contiene todos los servicios de competencias usados en el reporteador
 *
 */
@RestController
@RequestMapping(CompetenceReportsController.MAIN_PATH)
public class CompetenceReportsController {
	

	public static final String MAIN_PATH = "customreports/{companyId}/competences";
	
	public static final String GET_PING_HEALTH_CHECK = "/pingHealthCheck";
	
	public static final String EVALUATIONS = "/evaluations";	
	
	public static final String EVALUATION_ID = EVALUATIONS + "/{evaluationId}";
	
	public static final String SEMAPHORE = EVALUATION_ID + "/semaphore";
	
	public static final String AVG = EVALUATION_ID + "/avg";
	
	public static final String DIVISIONS = EVALUATION_ID + "/divisions";
	
	public static final String DIVISIONS_AVG = EVALUATION_ID + "/divisions/avg";
	
	public static final String MACROCOMPETENCES = EVALUATION_ID + "/macrocompetences";

	public static final String SUBSIDIARIES = EVALUATION_ID + "/subsidiaries";
	
	public static final String EXCEL_REPORT = EVALUATION_ID + "/report";
	
	public static final String REPORT_TEMPLATE = "/reporttemplate";
	
	public static final String EMPLOYEES = EVALUATION_ID + "/employees";
	
	public static final String JOBS = EVALUATION_ID + "/jobs";
	
	public static final String CALL_PROCEDURE = EVALUATION_ID + "/procedure";
	
	public static final String MODULES = "/modules";

	public static final String EMPLOYEE_DETAIL = EMPLOYEES + "/detail";
	
	public static final String EMPLOYEE_COMPETENCES = EMPLOYEES + "/{employeeId}";
	
	public static final String EMPLOYEE_BEHAVIORS = EMPLOYEE_COMPETENCES + "/behaviors";
	
	public static final String COMPETENCES_CONFIGURATION="/configurations";
	
	public static final String GENERAL_SUGGESTIONS=EMPLOYEE_COMPETENCES+"/suggestions";
	
	public static final String TEMPLATES = EVALUATION_ID + "/templates";
	
	public static final String SEMAPHORE_USERS="/semaphore/employees";

	
	
	@Autowired
	private CompetenceReportsService competenceReportsService;
	
	

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Ping Validator Health Check", notes = "Get and validate that the container its good and execute healthy", response = String.class, httpMethod = "GET")
	@GetMapping(GET_PING_HEALTH_CHECK)
	public ResponseEntity<String> getPingHealthCheck() {
		
		try {
			return ResponseEntity.ok().body("The module is response good");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Health Check Service", e);
		}
	}
	
	
	
	@ApiOperation(value = "Obtiene listado de evaluaciones de competencias de una empresa", response = CompetencesEvaluationDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EVALUATIONS)
	public List<CompetencesEvaluationDTO> getCompetencesEvaluationsByCompany(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "isLeader", value = "Indica si es colaborador o líder", required = false) @RequestParam(required = false) Boolean isLeader) {		
		return competenceReportsService.getCompetencesEvaluationsByCompany(companyId, subsidiariesAdmin, employeeId, isLeader);
	}	


	@ApiOperation(value = "Metodo que permite obtener los limites, colores y las etiquetas del semaforo de la evaluacion")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SEMAPHORE)
	public List<SemaphoreDTO> getEvaluationSemaphore(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId) {
		try {
			  return competenceReportsService.getSemaphore(evaluationId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	@ApiOperation(value = "Obtiene el promedio general de una evaluación de competencias")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(AVG)
	public Double getAvgCompetencesResults(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true)
			@RequestParam(required = true) String subsidiariesAdmin,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId, 
			@ApiParam(name = "relation", value = "filtrar por una relacion en particular", required = false) @RequestParam(required = false) String relation,
			@ApiParam(name = "calibrated", value = "Indica si el resultado debe ser el calibrado o el general", required = false) @RequestParam(required = false) Boolean calibrated) {
		try {
			 return competenceReportsService.getAvgCompetences(evaluationId, subsidiariesAdmin, employeeId, relation, calibrated);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
	
	@ApiOperation(value = "Obtiene las competencias de una evaluación con su respectivo promedio")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EVALUATION_ID)
	public List<ResultDTO> getResultsGroupByCompetences(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required = false) Long divisionId,
			@ApiParam(name = "subsidiaryId", value = "Identificador de la sede", required = false) @RequestParam(required = false) Long subsidiaryId,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "isLeader", value = "Indica si es colaborador o líder", required = false) @RequestParam(required = false) Boolean isLeader,
			@ApiParam(name = "calibrated", value = "Indica si el resultado debe ser el calibrado o el general", required = false) @RequestParam(required = false) Boolean calibrated) {
		try {
			 return competenceReportsService.getResultsGroupByCompetences(evaluationId, divisionId, subsidiaryId, null, null, subsidiariesAdmin, employeeId, isLeader, calibrated);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
		
	@ApiOperation(value = "Obtiene el porcentaje de cumplimiento de las competencias por departamento y líderes, según una evaluación")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(DIVISIONS)
	public List<CompetencesByDivisionsDTO> getResultsCompetencesByLeaders(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "showLeaders", value = "Determina si mostrar lideres o niveles de cargo", required = false) @RequestParam(required = true) boolean showLeaders,
			@ApiParam(name = "calibrated", value = "Indica si el resultado debe ser el calibrado o el general", required = false) @RequestParam(required = false) Boolean calibrated,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			  return competenceReportsService.getResultsCompetencesByDivisions(evaluationId, companyId, showLeaders, subsidiariesAdmin, calibrated);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	@ApiOperation(value = "Obtiene los departamentos que tienen resultados de una evaluación, con su respectivo promedio")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(DIVISIONS_AVG)
	public List<ResultDTO> getResultsGroupByDivisions(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId, 
			@ApiParam(name = "calibrated", value = "Indica si el resultado debe ser el calibrado o el general", required = false) @RequestParam(required = false) Boolean calibrated,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			  return competenceReportsService.getResultsGroupByDivision(evaluationId,subsidiariesAdmin, calibrated);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	@ApiOperation(value = "Obtiene las competencias de una evaluación con su respectivo promedio, agrupadas por macrocompetencia")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(MACROCOMPETENCES)
	public List<CategoryDTO> getCompetencesResultByMacrocompetence(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required = false) Long divisionId,
			@ApiParam(name = "calibrated", value = "Indica si el resultado debe ser el calibrado o el general", required = false) @RequestParam(required = false) Boolean calibrated,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			 return competenceReportsService.getCompetencesResultByMacrocompetence(evaluationId, employeeId, divisionId, subsidiariesAdmin, calibrated);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
		
	@ApiOperation(value = "Obtiene las sedes que tienen resultados de una evaluación, con su respectivo promedio")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SUBSIDIARIES)
	public List<ResultDTO> getResultsGroupBySubsidiaries(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "calibrated", value = "Indica si el resultado debe ser el calibrado o el general", required = false) @RequestParam(required = false) Boolean calibrated,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			  return competenceReportsService.getResultsGroupBySubsidiaries(evaluationId, subsidiariesAdmin, calibrated);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	@ApiOperation(value = "Obtiene reporte de excel de competencias")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(EXCEL_REPORT)
	public ResponseEntity<byte[]> getCompetencesReport(
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "companyId", value = "Identificador compañia", required = false) @PathVariable Long companyId,
			@ApiParam(name = "calibrated", value = "Indica si el resultado debe ser el calibrado o el general", required = false) @RequestParam(required = false) Boolean calibrated,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin,
			@RequestBody CompetencesExcelFiltersDTO filters){
		try {
			byte[] excelReport =  competenceReportsService.getCompetencesExcelReport(companyId,evaluationId,filters,subsidiariesAdmin, calibrated);				
			return new ResponseEntity<byte[]>(excelReport, HttpStatus.OK);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	} 
	
	
	@ApiOperation(value = "Obtiene el reporttemplate de una empresa")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(REPORT_TEMPLATE)
	public Long getResultTemplate(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId) {
		try {
			  return competenceReportsService.getReportTemplateByCompnay(companyId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	@ApiOperation(value = "Obtiene promedio de resultados por empleados para una evaluación de competencias", response = RecruitingPostulationStageDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EMPLOYEES)
	public <T>PageableResponse<T> getResultsGroupByEmployee(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "pageable", value = "Paginación", required = true) Pageable pageable,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required = false) Long divisionId,
			@ApiParam(name = "employeeName", value = "Nombre del empleado", required = false) @RequestParam(required = false) String employeeName,
			@ApiParam(name = "calibrated", value = "Indica si son resultados calibrados, por defecto son normales", required = false) @RequestParam(required = false) Boolean calibrated,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			return competenceReportsService.getResultsGroupByEmployee(companyId, evaluationId, pageable, divisionId, employeeName, subsidiariesAdmin, calibrated);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Obtiene promedio de competencia por cargo")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(JOBS)
	public <T>PageableResponse<T> getCompetencesByJob(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "pageable", value = "Paginación", required = true) Pageable pageable,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required = false) Long divisionId,
			@ApiParam(name = "competenceId", value = "Identificador de la competencia", required = false) @RequestParam(required = false) Long competenceId,
			@ApiParam(name = "calibrated", value = "Indica si son resultados calibrados, por defecto son normales", required = false) @RequestParam(required = false) Boolean calibrated,
			@ApiParam(name = "subsidiariesAdmin", value = "Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico", required = true) 
			@RequestParam(required = true) String subsidiariesAdmin) {
		try {
			return competenceReportsService.getCompetencesByJob(companyId, evaluationId, pageable, divisionId, competenceId, subsidiariesAdmin, calibrated);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Llama al procedimiento almacenado que recalcula la data de competencias")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(CALL_PROCEDURE)
	public void callStoredProcedure(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId) {
		try {
			 competenceReportsService.executeStoredProcedureCompetences(companyId, evaluationId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	@ApiOperation(value = "Obtiene listado de módulos activos de una empresa")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(MODULES)
	public Map<String, String> getActiveModulesByCompany(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId) {		
		return competenceReportsService.getActiveModulesByCompany(companyId);
	}	

	
	@ApiOperation(value = "Obtiene el detalle de las relaciones evaluadas y las competencias al colaborador")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EMPLOYEE_DETAIL)
	public Map<String, String> getDetailCollaborator(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = true) @RequestParam(required = true) Long employeeId) {
		
		try {
			return competenceReportsService.getDetailCollaborator(companyId, evaluationId, employeeId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	@ApiOperation(value = "Obtiene las competencias del colaborador")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EMPLOYEE_COMPETENCES)
	public <T>PageableResponse<T> getCompetencesCollaborator(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = true) @PathVariable(required = true) Long employeeId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "pageable", value = "Paginación", required = true) Pageable pageable,
			@ApiParam(name = "isLeader", value = "Indica si es colaborador o líder", required = false) @RequestParam(required = false) Boolean isLeader,
			@ApiParam(name = "orderType", value = "Ordenamiento ASC o DESC", required = true) @RequestParam(required = true) String orderType,
			@ApiParam(name = "relation", value = "filtrar por una relacion en particular", required = false) @RequestParam(required = false) String relation,
			@ApiParam(name = "calibrated", value = "Indica si son resultados calibrados, por defecto son normales", required = false) @RequestParam(required = false) Boolean calibrated,
			@ApiParam(name = "weight", value = "Indica si necesitamos los pesos de las competencias", required = false) @RequestParam(required = false) Boolean weight) {
		
		try {
			return competenceReportsService.getCompetencesCollaborator(companyId, evaluationId, employeeId, pageable, isLeader, orderType,relation,calibrated,weight);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Obtiene los comportamientos de una competencia con sus respectivas relaciones y resultado")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EMPLOYEE_BEHAVIORS)
	public List<BehaviorDetailDTO> getBehaviorsDetailByCompetence(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = true) @PathVariable(required = true) Long employeeId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "competenceId", value = "Identificador de la competencia", required = true) @RequestParam(required = true) Long competenceId,
			@ApiParam(name = "calibrated", value = "Indica si son resultados calibrados, por defecto son normales", required = false) @RequestParam(required = false) Boolean calibrated) {
		
		try {
			return competenceReportsService.getBehaviorsDetailByCompetence(companyId, evaluationId, employeeId, competenceId,calibrated);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene la configuración para saber si mostrar resultados")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(COMPETENCES_CONFIGURATION)
	public Map<String, Boolean> getBehaviorsDetailByCompetence(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId) {
		
		try {
			return competenceReportsService.getCompetenceResultsConfiguration(companyId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene los labels de los comentarios generales junto los respectivos comentarios realizados al colaborador en la evaluación")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(GENERAL_SUGGESTIONS)
	public List<QuestionCommentDTO> getGeneralSuggestionsByEvaluationAndEmployee(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = true) @PathVariable(required = true) Long employeeId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "relation", value = "Relación de un cuestionario", required = false) @RequestParam(required = false) String relation
			) {
		
		try {
			return competenceReportsService.getGeneralSuggestionsByEvaluationAndEmployee(companyId, evaluationId, employeeId, relation);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene id de plantilla de reporte de competencias")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(TEMPLATES)
	public Long getPdfReportTemplate(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId) {
		try {
			return competenceReportsService.getPdfReportTemplate(companyId, evaluationId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Obtiene la cantidad de empleados por semaforo")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(SEMAPHORE_USERS)
	public List<ResponseDTO> getSemaphoreWithUsersCount(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@RequestBody FiltersResultsDTO filters) {
		
		try {
			return competenceReportsService.getEmployeesBySempahore(companyId, filters);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
