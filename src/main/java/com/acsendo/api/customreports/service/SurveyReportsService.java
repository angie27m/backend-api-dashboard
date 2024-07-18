package com.acsendo.api.customreports.service;


import static com.acsendo.api.util.DataObjectUtil.getDouble;
import static com.acsendo.api.util.DataObjectUtil.getInteger;
import static com.acsendo.api.util.DataObjectUtil.getLong;
import static com.acsendo.api.util.DataObjectUtil.getString;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acsendo.api.climate.enumerations.SentimentType;
import com.acsendo.api.customReports.dao.SurveyResultsDAO;
import com.acsendo.api.customreports.dto.FilterDTO;
import com.acsendo.api.customreports.dto.KeywordClimateDTO;
import com.acsendo.api.customreports.dto.OpenQuestionResponseDTO;
import com.acsendo.api.customreports.dto.ResponseDTO;
import com.acsendo.api.customreports.dto.SurveyEvaluationDTO;
import com.acsendo.api.customreports.dto.SurveyQuestionDTO;
import com.acsendo.api.survey.enumerations.SurveyState;
import com.acsendo.api.survey.repository.PartakerRepository;
import com.acsendo.api.survey.repository.QuestionsRepository;
import com.acsendo.api.survey.repository.SurveyRepository;



/**
 * Servicio que contiene todos los métodos necesarios para obtener los resultados 
 * de las encuestas cortas para reporteador
 * */
@Service
public class SurveyReportsService {
	
	
	@Autowired
	private SurveyResultsDAO surveyResultsDAO;

	@Autowired
	private SurveyRepository surveyRepository;
	
	@Autowired
	private QuestionsRepository questionsRepository;
	
	@Autowired
	private PartakerRepository partakerRepository;
	
	/**
	 * Obtiene listado de encuestas por compañía
	 * @param companyId Identificador de la compañía
	 * @param subsidiary Sede a la que hace parte un rol subadmin
	 */
	public List<SurveyEvaluationDTO> getSurveyEvaluationsByCompany(Long companyId, String subsidiary) {
		List<Long> listSubsidiaries = null;
		if (subsidiary != null) {
			listSubsidiaries = Arrays.asList(subsidiary.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
		}
		List<SurveyEvaluationDTO> listSurveys = new ArrayList<SurveyEvaluationDTO>();
		List<Object[]> listSurveysRepository = null;
		//Consulta para traer las propiedades principales de las encuestas de una empresa
		if (listSubsidiaries == null) {
			listSurveysRepository = surveyRepository.findSurveysByCompany(companyId);	
		} else {
			listSurveysRepository = surveyRepository.findSurveysByCompanyWithSubsidiary(companyId, listSubsidiaries);
		}	
		
		if (listSurveysRepository != null && !listSurveysRepository.isEmpty()) {
			for (Object[] surv : listSurveysRepository) {
				SurveyEvaluationDTO surveyDTO = new SurveyEvaluationDTO();
				// Datos generales de la encuesta
				surveyDTO.setId(getLong(surv[0]));
				surveyDTO.setName(getString(surv[1]));
				surveyDTO.setCreatedDate((Date) surv[2]);
				surveyDTO.setSurveyState(SurveyState.valueOf((String) surv[3]));
				//Consulta para traer el conteo de preguntas que tiene una encuesta
				long countQuestions = questionsRepository.countQuestionBySurveyId(getLong(surv[0]));
				surveyDTO.setCountQuestions(countQuestions);
				//Consulta para traer el conteo de los participantes de una encuesta
				long countParticipants = subsidiary != null ? partakerRepository.countParticipantsBySurveyIdWithSubsidiary(getLong(surv[0]), listSubsidiaries)
						: partakerRepository.countParticipantsBySurveyId(getLong(surv[0]));
				surveyDTO.setCountParticipants(countParticipants);
				//Consulta para traer el conteo de los participantes que ya terminaron una encuesta
				long countParticipantsFinished = subsidiary != null ? partakerRepository.countParticipantsFinishedSurveyBySurveyIdWithSubsidiary(getLong(surv[0]), listSubsidiaries)
						: partakerRepository.countParticipantsFinishedSurveyBySurveyId(getLong(surv[0]));
				surveyDTO.setCountParticipantsFinished(countParticipantsFinished);
				//Porcentaje de respuesta
				surveyDTO.setResponseRate(countParticipantsFinished*100.00/countParticipants);
				if (countParticipants > 0) {
					// Tiempo promedio de respuesta
					Optional<String> optTimeResponse = partakerRepository.promDateInitialAndFinalResponseSurveys(getLong(surv[0]));
					String avgTime = optTimeResponse.isPresent() ? optTimeResponse.get() : "00:00:00";
					surveyDTO.setAverageTime(avgTime);
					listSurveys.add(surveyDTO);
				}
			}
		}
		
		return listSurveys;
	}
	
	
	
	/**
	 * Obtiene listado de preguntas de encuestas cortas junto con el resultado por opción de respuesta
	 * 
	 * @param surveyId Identificador de una encuesta corta
	 * @param divisionId Identificador del departamento
	 * 
	 */
	public List<SurveyQuestionDTO> getResultQuestionsAndOptionsResponses(Long surveyId, Long divisionId, String subsidiaries){
		
		
		List<SurveyQuestionDTO> questions=new ArrayList<SurveyQuestionDTO>();
		
		
		List<Object[]> list=surveyResultsDAO.getQuestionsResultsBySurvey(surveyId, divisionId, subsidiaries);
		
		
		list.stream().forEach(data->{
			
			Optional<SurveyQuestionDTO> optQ = questions.stream().filter(
					q-> q.getId().equals(getLong(data[0])))
					.findFirst();
			
			
			if(!optQ.isPresent()) {
				
				questions.add(createNewQuestion(data, surveyId, divisionId, subsidiaries));
				
			}else {
				
				Integer index = questions.indexOf(optQ.get());	
				ResponseDTO option=new ResponseDTO();
				option.setId(getLong(data[4]));
				option.setLabel(getString(data[5]));
				option.setCountResponses(getInteger(data[6]));
				option.setPercentage(getDouble(data[7]));
				questions.get(index).getOptionsResponses().add(option);
				
			}
		});
		
		return questions;
		
	}
	
	
	/**
	 * Método que crea el dto de una pregunta
	 * */
	private SurveyQuestionDTO createNewQuestion(Object[] data, Long surveyId, Long divisionId, String subsidiaries) {
		
		SurveyQuestionDTO questionDto=new SurveyQuestionDTO();
		questionDto.setId(getLong(data[0]));
		questionDto.setQuestion(getString(data[1]));
		questionDto.setType(getString(data[2]));
		questionDto.setQuestionCode(getString(data[3]));
		
		List<ResponseDTO> responses=new ArrayList<ResponseDTO>();
		ResponseDTO option=new ResponseDTO();
		option.setId(getLong(data[4]));
		option.setLabel(getString(data[5]));
		option.setCountResponses(getInteger(data[6]));
		option.setPercentage(getDouble(data[7]));
		responses.add(option);
		questionDto.setOptionsResponses(responses);
		
		 if(questionDto.getType().equals("DATA")) {
			 questionDto.setResponsesData(getResponsesByQuestionTypeData(surveyId,  questionDto.getId(), divisionId, subsidiaries));
		 }
		 
		 
		 return questionDto;
		 
	}
	
	
	
	/**
	 * 
	 *  Método que obtiene todas las respuestas de las preguntas del tipo DATA
	 *  @param surveyId  Identificador de la encuesta
	 *  @param questionId
	 *  @param divisionId
	 *  
	 * */
	private List<Map<String, String>> getResponsesByQuestionTypeData(Long surveyId, Long questionId, Long divisionId, String subsidiaries) {
		
		
		List<Map<String, String>> responsesData=new ArrayList<Map<String,String>>();
		
		List<Object[]> list=surveyResultsDAO.getResponsesByDataQuestion(surveyId,questionId, divisionId, subsidiaries);
		
		
		//Agrupamos por partakerId para meter todas las opciones de respuesta y respuesta en un mismo map
		Map<Long, List<Object[]>> group = list.stream().collect(Collectors.groupingBy(d-> getLong(d[0])));
		
		 for (Map.Entry<Long, List<Object[]>> entry : group.entrySet()) {
		      
			 Map<String, String> mapResponses=new HashMap<String, String>();
			 List<Object[]> objects=entry.getValue();
			 
			 objects.forEach(obj->{
				 mapResponses.put(getString(obj[1]), getString(obj[3]));
			 });
		
           responsesData.add(mapResponses);

		 }
		 
		 //Ordenamos por el primer label que trae la lista de BD, porque ese es el de prioridad 1
		 responsesData.sort((o1, o2) -> o1.get(getString(list.get(0)[1])).compareTo(o2.get(getString(list.get(0)[1]))));
		 return responsesData;
	}
	
	
	
	/**
	 * 
	 *  Método que devuelve todas las respuestas de tipo texto de una encuesta
	 *  @param surveyId  Identificador de la encuesta
	 *  @param questionId
	 *  @param divisionId
	 *  
	 *  @return List<OpenQuestionResponseDTO> Listado de todas las respuestas de tipo texto
	 * */
	public List<OpenQuestionResponseDTO> getTextResponsesBySurvey(Long surveyId, String word, Long questionId, Long divisionId, String subsidiaries, String questionCode){
		
		List<OpenQuestionResponseDTO> responses=new ArrayList<OpenQuestionResponseDTO>();
		
		List<Object[]> responsesTextType=surveyResultsDAO.getResponsesByTextQuestion(surveyId, word, questionId,divisionId, subsidiaries, questionCode);
		
		if(responsesTextType.size()>0) {
	      responses=responsesTextType.stream().map(rt->new OpenQuestionResponseDTO(getLong(rt[1]), getString(rt[3]), rt[2] !=null? SentimentType.valueOf(getString(rt[2])):null, getLong(rt[0]))).collect(Collectors.toList());
		}
		
		return responses;
	}
	
	
	
	/**
	 * Método que obtiene los departamentos con respuestas de una encuesta
	 * @param surveyId Identificador de la encuesta
	 * @param subsidiaries Identificador de la(s) sedes a filtrar
	 * 
	 * */
	public List<FilterDTO> getDivisionsWithResponses(Long surveyId, String subsidiaries) {
		List<FilterDTO> divisions = new ArrayList<FilterDTO>();

		
		List<Object[]>	list = surveyResultsDAO.getDivisionsWithResponses(surveyId, subsidiaries);
		 
			if (list != null && !list.isEmpty()) {

				Function<Object[], FilterDTO> questionMapper = mQuestion -> {
					FilterDTO question = new FilterDTO();
					question.setId(getLong(mQuestion[0]));
					question.setName(getString(mQuestion[1]));
					return question;
				};

				divisions = list.stream().map(questionMapper).collect(Collectors.toList());

			}
		

		return divisions;
	}
	
	
	/**
	 * Método que obtiene los resultados agrupados por sentimiento
	 * @param evaluationId Identificador de la encuesta
	 * @param word Palabra a filtrar en las preguntas abiertas
	 * @param questionId Identificador de la pregunta abierta a filtrar
	 * @param divisionId Identificador del departamento a filtrar
	 * @param subsidiaries 
	 * 
	 * */
	public List<ResponseDTO> getResultsSentiments(Long surveyId, String word, Long questionId, Long divisionId, String subsidiaries){
		
		List<ResponseDTO> sentiments=new ArrayList<ResponseDTO>();
		
	
			List<Object[]> list=surveyResultsDAO.getSentimentsBySurvey(surveyId, word, questionId, divisionId, subsidiaries);
			Double total = list.stream().mapToDouble(dto->((BigInteger)dto[1]).intValue()).sum();
			Arrays.asList(SentimentType.NEGATIVE, SentimentType.MIXED, SentimentType.NEUTRAL, SentimentType.POSITIVE).forEach(k->{
				Optional<Object[]> sentim=list.stream().filter(l->(getString(l[0]).equals(k.toString()))).findFirst();
				ResponseDTO dto=new ResponseDTO();
				if(sentim.isPresent()) {
					dto.setLabel(k.toString());
					dto.setCountResponses(getInteger(sentim.get()[1]));
					dto.setPercentage((dto.getCountResponses()*100)/total);
				}else {
					dto.setLabel(k.toString());
					dto.setCountResponses(0);
					dto.setPercentage(0.0);
				}
				
				sentiments.add(dto);
				
			});
		
		return sentiments;
	
	}
	
	
	
	
	/**
	 * Obtiene las palabras clave extraídas de los comentarios hechos en preguntas tipo texto de una encuesta
	 * @param evaluationId Identificador evaluación de clima
	 * @param questionId Identificador de una pregunta tipo parrafo
	 * @param divisionId Identificador del departamento
	 * @param subsidiaries 
	 */
	@Transactional
	public List<KeywordClimateDTO> getKeywordsBySurvey(Long surveyId, Long questionId, Long divisionId, String subsidiaries) {
		
		List<KeywordClimateDTO> keyWordList = new ArrayList<KeywordClimateDTO>();
		List<Object[]> optList = surveyResultsDAO.getKeyWordsBySurvey(surveyId, questionId, divisionId, subsidiaries);
		
		if (optList.size()>0) {

			Long totalWords = optList.stream().mapToLong(t-> getLong(t[2])).sum();
			for (Object[] keyword : optList) {
				KeywordClimateDTO  dto = new KeywordClimateDTO();
				dto.setWord(keyword[0].toString());
				dto.setPercentage((double)(getInteger(keyword[2])*100.00/totalWords));
				dto.setType(keyword[1].toString());
				dto.setTotalCount(totalWords.intValue());
				//Consulta las respuestas que contienen cada palabra y su categoría de sentimiento
				List<List<Long>> optSentimentWord = surveyResultsDAO.findSentimentsResultsByResponseText(surveyId, dto.getWord(), divisionId, subsidiaries);
				
				if (optSentimentWord == null || optSentimentWord.isEmpty())
					continue;
				
				List<Long> sentimentWord = optSentimentWord.get(0);
				if(sentimentWord != null && !sentimentWord.isEmpty() && (sentimentWord.get(0).intValue()) == 0)
				{
					totalWords -= dto.getTotalCount();
					continue;
				}
				
				int totalCount = (sentimentWord.get(0).intValue());
				int countPositive = (sentimentWord.get(1).intValue());
				dto.setPositivePercentage((double)(countPositive*100/totalCount));
				int countNeutral = (sentimentWord.get(2).intValue());
				dto.setNeutralPercentage((double)(countNeutral*100/totalCount));
				int countMixed = (sentimentWord.get(3).intValue());
				dto.setMixedPercentage((double)(countMixed*100/totalCount));
				int countNegative = (sentimentWord.get(4).intValue());
				dto.setNegativePercentage((double)(countNegative*100/totalCount));
				
				keyWordList.add(dto);
			}
			
			//for (KeywordClimateDTO keyword : keyWordList)
				//keyword.setPercentage(((double) (keyword.getTotalCount() * 100))/ totalWords );
		}
		return keyWordList;
	}
	

}
