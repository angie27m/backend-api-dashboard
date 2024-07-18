package com.acsendo.api.customreports.controller;

import java.util.HashMap;
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

import com.acsendo.api.climate.model.ClimateCompanyEvaluation;
import com.acsendo.api.customreports.dto.CategoryDTO;
import com.acsendo.api.customreports.dto.ClimateExcelFiltersDTO;
import com.acsendo.api.customreports.dto.CompareResultsDTO;
import com.acsendo.api.customreports.dto.FilterDTO;
import com.acsendo.api.customreports.dto.FiltersClimateDTO;
import com.acsendo.api.customreports.dto.FiltersCompareClimateDTO;
import com.acsendo.api.customreports.dto.KeywordClimateDTO;
import com.acsendo.api.customreports.dto.OpenQuestionResponseDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.dto.SemaphoreDTO;
import com.acsendo.api.customreports.dto.ResponseDTO;
import com.acsendo.api.customreports.dto.SociodemographicDTO;
import com.acsendo.api.customreports.service.ClimateReportsService;
import com.acsendo.api.hcm.dto.PageableResponse;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * Contiene todos los servicios de clima usados en el reporteador
 *
 */
@RestController
@RequestMapping(ClimateReportsController.MAIN_PATH)
public class ClimateReportsController {

	public static final String MAIN_PATH = "customreports/{companyId}/climate";

	public static final String EVALUATIONS = "/evaluations";

	public static final String EVALUATION_ID = EVALUATIONS + "/{evaluationId}";
	
	public static final String SATISFACTION = EVALUATION_ID + "/satisfaction";
	
	public static final String FACTORS= EVALUATION_ID + "/factors";
	
	public static final String AVG= EVALUATION_ID + "/avg";
	
	public static final String DIMENSIONS = EVALUATION_ID + "/dimensions";
	
	public static final String DIVISIONS = EVALUATION_ID + "/divisions";
	
	public static final String DIVISIONS_HEATMAP = DIVISIONS+ "/heatmap";
	
	public static final String DETAIL_FACTORS=FACTORS+"/detail";
	
	public static final String DETAIL_DIMENSIONS=DIMENSIONS+"/detail";
	
	public static final String QUESTIONS=EVALUATION_ID+"/questions";
	
	public static final String SUBSIDIARIES = EVALUATION_ID + "/subsidiaries";
	
	public static final String GROUPED_RESULTS = EVALUATIONS + "/groupedresults";
	
	public static final String DIVISIONS_ALL = DIVISIONS+"/all";
	
	public static final String SEMAPHORE = EVALUATION_ID+"/semaphore";
	
	public static final String CALL_PROCEDURE = EVALUATION_ID + "/procedure";
	
	public static final String TYPES=EVALUATION_ID+"/types";
	
	public static final String ENPS=EVALUATION_ID+"/enps";
	
	public static final String RESULTS_KEYWORDS = EVALUATION_ID + "/keywords";
	
	public static final String QUESTIONS_OPEN=QUESTIONS+"/open";
	
	public static final String SENTIMENT=EVALUATION_ID+"/sentiments";
	
	public static final String QUESTIONS_OPEN_DIVISIONS=QUESTIONS_OPEN+"/divisions";
	
	public static final String RESPONSES=QUESTIONS_OPEN+"/responses";

	public static final String SOCIODEMOGRAPHIC = EVALUATION_ID + "/sociodemographic";
	
	public static final String EXCEL_REPORT = EVALUATION_ID + "/report";

	public static final String LEADER_EXCEL_REPORT = EVALUATION_ID + "/reportleader";
	
	
	@Autowired
	private ClimateReportsService climateReportsService;

	@ApiOperation(value = "Obtiene listado de evaluaciones de clima de una empresa", response = ClimateCompanyEvaluation.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EVALUATIONS)
	public List<ClimateCompanyEvaluation> getEvaluationsCompany(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado líder", required = false) @RequestParam(required = false)  Long employeeId,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false)  String subsidiary) {
		
		try {
			 return climateReportsService.getClimateEvaluationsByCompany(companyId, employeeId, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene los porcentajes de respuesta de las opciones de una evaluación de clima", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SATISFACTION)
	public List<ResultDTO> getSatisfactionResultsByClimateEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true) Boolean isEnps,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary) {
		
		try {
			 return climateReportsService.getSatisfactionResultsByClimateEvaluation(evaluationId, isEnps, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene los resultados de los factores de un modelo de evaluación de clima", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(FACTORS)
	public  List<ResultDTO> getfactorsByClimateEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true)  Boolean isEnps,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required = false)  Long divisionId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado líder", required = false) @RequestParam(required = false)  Long employeeId,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary) {
		
		try {
			 return climateReportsService.getFactorsByModelId(evaluationId, isEnps, divisionId, employeeId, subsidiary, companyId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Obtiene el promedio general de una evaluación de clima , o el resultado para un líder", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(AVG)
	public  Double getAvgClimateEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado líder", required = false) @RequestParam(required = false)  Long employeeId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true)  Boolean isEnps, 
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary
			) {
		try {
			 return climateReportsService.getAvgClimate(evaluationId, employeeId, isEnps, subsidiary, companyId, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene los resultados de las dimensiones de un modelo de evaluación de clima", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(DIMENSIONS)
	public  List<ResultDTO> getDimensionsByClimateEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true)  Boolean isEnps,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required = false)  Long divisionId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado líder", required = false) @RequestParam(required = false)  Long employeeId, 
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary) {
		
		try {
			 return climateReportsService.getDimensionsByClimateEvaluation(evaluationId, isEnps, divisionId, employeeId, subsidiary, companyId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Metodo que obtiene los departamentos que tienen resultados por evaluación")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(DIVISIONS)
	public List<FilterDTO> getDivisionsByClimateEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true)  Boolean isEnps) {
		try {
			  return climateReportsService.getDivisionsByClimateEvaluation(companyId, evaluationId, isEnps);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	@ApiOperation(value = "Obtiene el detalle de factores de un modelo de evaluación de clima", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(DETAIL_FACTORS)
	public  <T>PageableResponse<T> getFactorsDetail(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "pageable", value = "Paginación", required = true) Pageable pageable,
			@RequestBody FiltersClimateDTO filters) {
		
		try {
			 return climateReportsService.getFactorsOrDimensionsDetail(companyId, evaluationId, filters, pageable, false, employeeId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene el detalle de las dimensiones de un modelo de evaluación de clima", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(DETAIL_DIMENSIONS)
	public  <T>PageableResponse<T> getDimensionsDetail(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "pageable", value = "Paginación", required = true) Pageable pageable,
			@RequestBody FiltersClimateDTO filters
		 ) {
		
		try {
			return climateReportsService.getFactorsOrDimensionsDetail(companyId, evaluationId, filters, pageable, true, employeeId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Obtiene el detalle de las preguntas pertenecientes a un factor o dimensión de un modelo de evaluación de clima", response = CategoryDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(QUESTIONS)
	public  List<CategoryDTO> getDimensionsDetail(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId,
			@RequestBody FiltersClimateDTO filters) {
		
		try {
			return climateReportsService.getQuestionsByDimensionOrFactor(companyId, evaluationId, filters, employeeId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Obtiene los resultados de los departamentos ", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(DIVISIONS_ALL)
	public  List<ResultDTO> getDivisionsResultsByEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true)  Boolean isEnps) {
		
		try {
			 return climateReportsService.getDivisionsWithResultsByClimateEvaluation(evaluationId, isEnps);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Metodo que obtiene las sedes que tienen resultados en una evaluación de clima")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SUBSIDIARIES)
	public List<FilterDTO> getSubsidiariesByClimateEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true)  Boolean isEnps) {
		try {
			  return climateReportsService.getSubsidiariesByClimateEvaluation(companyId, evaluationId, isEnps);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	@ApiOperation(value = "Obtiene los resultados de los factores o dimensiones que son comunes entre 2 evaluaciones de clima", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(GROUPED_RESULTS)
	public CompareResultsDTO getGroupedResultsBetweenClimateEvaluations(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "typeFilter", value = "Tipo de Filtro: DIMENSION o FACTOR", required = true) @RequestParam String typeFilter,
			@RequestBody FiltersCompareClimateDTO filters) {		
		try {
			 return climateReportsService.getGroupedResultsBetweenClimateEvaluations(companyId, typeFilter, filters);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Metodo que obtiene el semáforo de una evaluación de clima")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SEMAPHORE)
	public List<SemaphoreDTO> getSemaphoreByClimateEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true)  Boolean isEnps
			) {
		try {
			  return climateReportsService.getSemaphore(evaluationId, isEnps);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	@ApiOperation(value = "Llama al procedimiento almacenado que recalcula la data de clima")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(CALL_PROCEDURE)
	public void callStoredProcedure(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId) {
		try {
			climateReportsService.executeStoredProcedureClimate(companyId, evaluationId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Obtiene los departamentos con el promedio de sus factores o dimensiones ", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(DIVISIONS_HEATMAP)
	public  <T>PageableResponse<T> getDivisionsWithFactorsOrDimensionsAvg(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "pageable", value = "Paginación", required = true) Pageable pageable,
			@RequestBody FiltersClimateDTO filters
		 ) {
		
		try {
			return climateReportsService.getDivisionsWithFactorsOrDimensionsAvg(companyId, evaluationId, filters, pageable);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene los tipos de la evaluación: enps y/o clima")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(TYPES)
	public Map<String, String> getTypeEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId) {
		try {
			Map<String,String> map=new HashMap<String,String>();
			map.put("type",climateReportsService.getTypesEvaluationClimate(companyId, evaluationId));
			return map ;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = " Método que devuelve un  resultado  con el porcentaje de promotores, neutros y detractores", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(ENPS)
	public  List<ResultDTO> getEnpsTypes(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary){
			
		try {
			 return climateReportsService.getResultEnpsGroupByTypes(companyId, evaluationId, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Obtiene las palabras clave extraídas de los comentarios hechos en preguntas abiertas de una evaluación", response = KeywordClimateDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(RESULTS_KEYWORDS)
	public List<KeywordClimateDTO> getKeywordsByEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary) {
		try {
			return climateReportsService.getClimateKeywordsByEvaluation(evaluationId, subsidiary);	
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	@ApiOperation(value = "Obtiene las preguntas abiertas de una evaluación", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(QUESTIONS_OPEN)
	public List<FilterDTO> getOpenQuestions(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "word", value = "Palabra a buscar", required = false) @RequestParam(required=false) String word,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary
			) {
		try {
			return climateReportsService.getClimateModelQuestionDTOByModelId(evaluationId, word, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);		
		}

	}
	
	@ApiOperation(value = "Obtiene los resultados de sentimientos de preguntas abiertas", response = ResponseDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SENTIMENT)
	public List<ResponseDTO> getSentiments(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "word", value = "Palabra a buscar", required = false) @RequestParam(required=false)  String word,
			@ApiParam(name = "questionId", value = "Identificador de la pregunta abierta a buscar", required = false) @RequestParam(required=false) Long questionId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required=false) Long divisionId,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary) {
		try {
			return climateReportsService.getResultsSentiments(evaluationId, word, questionId, divisionId, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	@ApiOperation(value = "Obtiene los departamentos con preguntas abiertas de una evaluación", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(QUESTIONS_OPEN_DIVISIONS)
	public List<FilterDTO> getDivisionsByOpenQuestions(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "word", value = "Palabra a buscar", required = false) @RequestParam(required=false) String word,
			@ApiParam(name = "questionId", value = "Identificador de la pregunta abierta a buscar", required = false) @RequestParam(required=false) Long questionId,
			@ApiParam(name = "subsidiary", value = "Id(s) de las sedes", required = false) @RequestParam(required=false) String subsidiary
			) {
		try {
			return climateReportsService.getDivisionsByOpenQuestions(evaluationId, word, questionId, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		

	}	
	
	@ApiOperation(value = "Obtiene los resultados de preguntas sociodemográficas de una evaluación", response = SociodemographicDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SOCIODEMOGRAPHIC)
	public List<SociodemographicDTO> getSociodemographicResultsByEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary) {
		try {
			return climateReportsService.getSociodemographicResultsByEvaluation(evaluationId, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene las respuestas de las preguntas abiertas de una evaluación", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(RESPONSES)
	public List<OpenQuestionResponseDTO> getResponsesByOpenQuestions(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "word", value = "Palabra a buscar", required = false) @RequestParam(required=false)  String word,
			@ApiParam(name = "questionId", value = "Identificador de la pregunta abierta a buscar", required = false) @RequestParam(required=false) Long questionId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required=false) Long divisionId,
			@ApiParam(name = "subsidiary", value = "Id(s) de las sedes", required = false) @RequestParam(required=false) String subsidiary) {
		try {
			return climateReportsService.getOpenResponsesByEvaluation(evaluationId, word, questionId, divisionId, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "Obtiene reporte de excel de clima")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(EXCEL_REPORT)
	public ResponseEntity<byte[]> getClimateExcelReport(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = false) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true) Boolean isEnps,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary, 
			@RequestBody ClimateExcelFiltersDTO filters){
		try {
			byte[] excelReport = climateReportsService.getClimateExcelReport(companyId, evaluationId, filters, isEnps, subsidiary);			
			return new ResponseEntity<byte[]>(excelReport, HttpStatus.OK);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	} 
	
	@ApiOperation(value = "Obtiene reporte de excel de clima para líderes")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(LEADER_EXCEL_REPORT)
	public ResponseEntity<byte[]> getClimateLeaderExcelReport(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = false) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación", required = true) @PathVariable Long evaluationId,
			@ApiParam(name = "isEnps", value = "Indica si es de clima o NPS", required = true) @RequestParam(required = true) Boolean isEnps,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = true) @RequestParam(required = true) Long employeeId){
		try {
			byte[] excelReport = climateReportsService.getClimateLeaderExcelReport(companyId, evaluationId, isEnps, employeeId);			
			return new ResponseEntity<byte[]>(excelReport, HttpStatus.OK);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	} 

}
