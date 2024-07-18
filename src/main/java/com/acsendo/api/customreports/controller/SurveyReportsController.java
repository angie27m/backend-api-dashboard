package com.acsendo.api.customreports.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acsendo.api.customreports.dto.FilterDTO;
import com.acsendo.api.customreports.dto.KeywordClimateDTO;
import com.acsendo.api.customreports.dto.OpenQuestionResponseDTO;
import com.acsendo.api.customreports.dto.ResponseDTO;
import com.acsendo.api.customreports.dto.SurveyEvaluationDTO;
import com.acsendo.api.customreports.dto.SurveyQuestionDTO;
import com.acsendo.api.customreports.service.SurveyReportsService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * Contiene todos los servicios de encuestador para reportes

 *
 */
@RestController
@RequestMapping(SurveyReportsController.MAIN_PATH)
public class SurveyReportsController {
	

	public static final String MAIN_PATH = "customreports/{companyId}/survey";	
	
	public static final String EVALUATIONS = "/evaluations";

	public static final String EVALUATION_ID = EVALUATIONS + "/{evaluationId}";	

	public static final String SURVEY= "/{surveyId}";

	public static final String SURVEY_QUESTIONS= SURVEY+"/questions";
	
	public static final String SURVEY_QUESTIONS_TEXTS= SURVEY_QUESTIONS+"/texts";
	
	public static final String SURVEY_DIVISIONS=SURVEY+ "/divisions";
	
	public static final String SENTIMENT= SURVEY+"/sentiments";
	
	public static final String RESULTS_KEYWORDS = SURVEY + "/keywords";
	
	@Autowired
	private SurveyReportsService surveyReportsService;
	
		
	@ApiOperation(value = "Obtiene listado de encuestas cortas de una empresa", response = SurveyEvaluationDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EVALUATIONS)
	public List<SurveyEvaluationDTO> getSurveyEvaluationsByCompany(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "subsidiary", value = "Sede a la que hace parte un rol subadmin", required = false) @RequestParam(required = false)  String subsidiary) {
		
		try {
			 return surveyReportsService.getSurveyEvaluationsByCompany(companyId, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@ApiOperation(value = "Obtiene listado de preguntas de encuestas cortas junto con el resultado por opción de respuesta", response = SurveyQuestionDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SURVEY_QUESTIONS)
	public List<SurveyQuestionDTO> getQuestionsBySurvey(@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "surveyId", value = "Identificador de la encuesta", required = true) @PathVariable Long surveyId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required=false) Long divisionId,
			@ApiParam(name = "subsidiary", value = "Id(s) de las sedes", required = false) @RequestParam(required=false) String subsidiary
			
			) {		
	 
		return surveyReportsService.getResultQuestionsAndOptionsResponses(surveyId, divisionId, subsidiary);
		
	}
	
	
	@ApiOperation(value = "Obtiene listado de respuestas de tipo texto de una encuesta", response = OpenQuestionResponseDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SURVEY_QUESTIONS_TEXTS)
	public List<OpenQuestionResponseDTO> getTextResponses(@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "surveyId", value = "Identificador de la encuesta", required = true) @PathVariable Long surveyId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required=false) Long divisionId,
			@ApiParam(name = "subsidiary", value = "Id(s) de las sedes", required = false) @RequestParam(required=false) String subsidiary,
			@ApiParam(name = "word", value = "Palabra a buscar", required = false) @RequestParam(required=false)  String word,
			@ApiParam(name = "questionId", value = "Identificador de la pregunta abierta a buscar", required = false) @RequestParam(required=false) Long questionId,
			@ApiParam(name = "code", value = "Identifica el tipo de pregunta text(TEXTLONG o TEXTSHORT)", required = false) @RequestParam(required=false) String code

			) {		
	 
		return surveyReportsService.getTextResponsesBySurvey(surveyId,word,questionId, divisionId, subsidiary, code);
		
	}
	
	
	@ApiOperation(value = "Obtiene el listado de departamentos con respuestas de una encuesta", response = FilterDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SURVEY_DIVISIONS)
	public List<FilterDTO> getDivisionsWithResponses(@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "surveyId", value = "Identificador de la encuesta", required = true) @PathVariable Long surveyId,
			@ApiParam(name = "subsidiary", value = "Id(s) de las sedes", required = false) @RequestParam(required=false) String subsidiary
			
			) {		
	 
		return surveyReportsService.getDivisionsWithResponses(surveyId, subsidiary);
		
	}
	
	
	@ApiOperation(value = "Obtiene los resultados de sentimientos de preguntas abiertas", response = ResponseDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SENTIMENT)
	public List<ResponseDTO> getSentiments(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "surveyId", value = "Identificador de la encuesta", required = true) @PathVariable Long surveyId,
			@ApiParam(name = "word", value = "Palabra a buscar", required = false) @RequestParam(required=false)  String word,
			@ApiParam(name = "questionId", value = "Identificador de la pregunta abierta a buscar", required = false) @RequestParam(required=false) Long questionId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required=false) Long divisionId,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary) {
		try {
			return surveyReportsService.getResultsSentiments(surveyId, word, questionId, divisionId, subsidiary);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	@ApiOperation(value = "Obtiene las palabras clave extraídas de los comentarios hechos en preguntas de tipo texto de una encuesta", response = KeywordClimateDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(RESULTS_KEYWORDS)
	public List<KeywordClimateDTO> getKeywordsBySurvey(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "surveyId", value = "Identificador de la encuesta", required = true) @PathVariable Long surveyId,
			@ApiParam(name = "questionId", value = "Identificador de la pregunta abierta a buscar", required = false) @RequestParam(required=false) Long questionId,
			@ApiParam(name = "divisionId", value = "Identificador del departamento", required = false) @RequestParam(required=false) Long divisionId,
			@ApiParam(name = "subsidiary", value = "subsidiaries ids cuando corresponda", required = false) @RequestParam(required = false) String subsidiary) {
		try {
			return surveyReportsService.getKeywordsBySurvey(surveyId, questionId, divisionId, subsidiary);	
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	

}
