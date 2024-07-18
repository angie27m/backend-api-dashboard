package com.acsendo.api.customreports.service;

import static com.acsendo.api.util.DataObjectUtil.getDouble;
import static com.acsendo.api.util.DataObjectUtil.getInteger;
import static com.acsendo.api.util.DataObjectUtil.getLong;
import static com.acsendo.api.util.DataObjectUtil.getString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acsendo.api.climate.dao.ClimateResultsDAO;
import com.acsendo.api.climate.enumerations.SentimentType;
import com.acsendo.api.climate.factory.ClimateCompanyEvaluationFactory;
import com.acsendo.api.climate.model.ClimateCompanyEvaluation;
import com.acsendo.api.climate.model.ClimateConfiguration;
import com.acsendo.api.climate.model.ClimateDemographicField;
import com.acsendo.api.climate.model.ClimateDemographicOption;
import com.acsendo.api.climate.model.ClimateModel;
import com.acsendo.api.climate.repository.ClimateConfigurationRepository;
import com.acsendo.api.climate.repository.ClimateDemographicFieldRepository;
import com.acsendo.api.climate.repository.ClimateDemographicOptionRepository;
import com.acsendo.api.climate.repository.ClimateDemographicResponseRepository;
import com.acsendo.api.climate.repository.ClimateEvaluationKeywordRepository;
import com.acsendo.api.climate.repository.ClimateEvaluationRepository;
import com.acsendo.api.company.repository.CompanyRepository;
import com.acsendo.api.customReports.dao.ClimateResultsRedshiftDAO;
import com.acsendo.api.customreports.dto.CategoryDTO;
import com.acsendo.api.customreports.dto.ClimateExcelFiltersDTO;
import com.acsendo.api.customreports.dto.CompareResultsDTO;
import com.acsendo.api.customreports.dto.FilterDTO;
import com.acsendo.api.customreports.dto.FiltersClimateDTO;
import com.acsendo.api.customreports.dto.FiltersCompareClimateDTO;
import com.acsendo.api.customreports.dto.KeywordClimateDTO;
import com.acsendo.api.customreports.dto.OpenQuestionResponseDTO;
import com.acsendo.api.customreports.dto.QuestionDetailDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.dto.SemaphoreDTO;
import com.acsendo.api.customreports.dto.ResponseDTO;
import com.acsendo.api.customreports.dto.SociodemographicDTO;
import com.acsendo.api.customreports.util.CustomReportsClimateExcelHandler;
import com.acsendo.api.evaluation.model.Climate;
import com.acsendo.api.evaluation.model.FormEvaluation;
import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Evaluation;
import com.acsendo.api.hcm.model.JobRole;
import com.acsendo.api.hcm.repository.EvaluationRepository;
import com.acsendo.api.hcm.repository.JobRoleRepository;

@Service
public class ClimateReportsService {

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private EvaluationRepository evaluationRepository;

	@Autowired
	private ClimateResultsDAO climateResultsDao;
	
	@Autowired
	private ClimateResultsRedshiftDAO climateRedshiftDao;
	
	@Autowired
	private ClimateConfigurationRepository climateConfigurationRepository;
	
	@Autowired
	private  ClimateEvaluationRepository climateEvaluationRepository; 
	
	@Autowired
	ClimateEvaluationKeywordRepository climateEvaluationKeywordRepository;
	
	@Autowired
	ClimateDemographicFieldRepository climateDemographicFieldRepository;
	
	@Autowired
	ClimateDemographicOptionRepository climateDemographicOptionRepository;
	
	@Autowired
	ClimateDemographicResponseRepository climateDemographicResponseRepository;
	
	@Autowired
	private CustomReportsClimateExcelHandler excelHandler;
	
	@Autowired
	private JobRoleRepository jobRoleRepository;
	
		
	/**
	 * Método que retorna todas las evaluaciones de clima de la compañia
	 * 
	 * @param companyId Identificador de la compañía
	 */
	public List<ClimateCompanyEvaluation> getClimateEvaluationsByCompany(Long companyId, Long employeeId,  String subsidiaries) {

		Company company = this.companyRepository.getOne(companyId);
		List<ClimateCompanyEvaluation> listEvaluations = new ArrayList<ClimateCompanyEvaluation>();

		//Se consultan evaluaciones de clima de ambos modelos
		Optional<List<FormEvaluation>> listOptEvaluationsOldModel = evaluationRepository.findAllActiveClimateByCompany(company);
		
		Optional<List<Climate>> listOptEvaluationsNewModel= null;
		if(employeeId!=null) {
			listOptEvaluationsNewModel = evaluationRepository.findClimateEvaluationsToshowLeader(company);
		}else {
			listOptEvaluationsNewModel = evaluationRepository.findClimateEvaluationsByCompany(company);
		}
	

		if (listOptEvaluationsOldModel.isPresent()) {
			List<FormEvaluation> evals = listOptEvaluationsOldModel.get();			
			listEvaluations.addAll(evals.stream()
					.map(evaluation -> ClimateCompanyEvaluationFactory.getClimateCompanyEvaluation(evaluation,
							climateResultsDao.getClimateCompanyEvaluationStatusByEvaluationId(evaluation.getId(), subsidiaries), false))
					.collect(Collectors.toList()));
			listEvaluations.stream().forEach(list -> list.setTypeModel("OLD"));
		} 
		if (listOptEvaluationsNewModel!= null && listOptEvaluationsNewModel.isPresent()) {	

			List<Climate> evals = listOptEvaluationsNewModel.get();					
			List<ClimateCompanyEvaluation> listNewEvaluations = new ArrayList<ClimateCompanyEvaluation>();
			listNewEvaluations = evals.stream()
			.map(evaluation -> ClimateCompanyEvaluationFactory.getClimateCompanyEvaluation(evaluation,
					climateResultsDao.getClimateCompanyEvaluationStatusByModelId(evaluation.getId(), evaluation.getModel().getId(), subsidiaries), true))
			.collect(Collectors.toList());
			listNewEvaluations.stream().forEach(list -> list.setTypeModel("NEW"));
			listEvaluations.addAll(listNewEvaluations);

		}
		
		climateRedshiftDao.createClimateCompanyTable(companyId);
		// Consultamos los tipos de pregunta que tiene cada evaluación, ENPS - CLIMATE - BOTH
		// y obtenemos su respectivo promedio
		listEvaluations.stream().forEach(evaluation -> {
			if (evaluation.getModel() != null) {
				String type = getTypesEvaluationClimate(companyId, evaluation.getId());
				if (type.equals("CLIMATE")) {
					evaluation.setAverageClimate(getAvgClimate(evaluation.getId(), employeeId, false, subsidiaries, companyId, null));
				} else if (type.equals("ENPS")) {
					if (evaluation.getModel().getId() != null) {
						Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluation.getModel().getId());
						if (configOpt.isPresent() && configOpt.get().getCalculationType() != null) {
							evaluation.setAverageEnps(getAvgClimate(evaluation.getId(), employeeId, true, subsidiaries, companyId, null));
						}	
					}								
				} else {
					evaluation.setAverageClimate(getAvgClimate(evaluation.getId(), employeeId, false, subsidiaries, companyId, null));
					if (evaluation.getModel().getId() != null) {
						Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluation.getModel().getId());
						if (configOpt.isPresent() && configOpt.get().getCalculationType() != null) {
							evaluation.setAverageEnps(getAvgClimate(evaluation.getId(), employeeId, true, subsidiaries, companyId, null));
						}
					}								
				}
			}		
		});
		// Ordenamiento descendente según el id de creación
		listEvaluations = listEvaluations.stream().sorted((ev1, ev2) -> ev2.getId().compareTo(ev1.getId())).collect(Collectors.toList());
		
		return listEvaluations;
	}

	/**
	 * Obtiene los porcentajes de respuesta de las opciones de un modelo de clima
	 * @param evaluationId Identificador de evaluación
	 */
	public List<ResultDTO> getSatisfactionResultsByClimateEvaluation(Long evaluationId, Boolean isEnps, String subsidiaries) {
		List<ResultDTO> results = new ArrayList<ResultDTO>();

		List<Object[]> list = new ArrayList<Object[]>();
		
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			if (evaluationNewModel != null) {
				list = climateRedshiftDao.getSatisfactionResultsByEvaluationClimate(evaluationNewModel.getCompany().getId(), evaluationId,null, isEnps, subsidiaries);
			}
		} else {
			list = climateRedshiftDao.getSatisfactionResultsByEvaluationClimate(evaluationOldModel.getCompany().getId(), evaluationId,null, isEnps, subsidiaries);
		}	
		
		if (list != null) {

			Function<Object[], ResultDTO> mapper = data -> {
				ResultDTO result = new ResultDTO();
				result.setId(getLong(data[0]));
				result.setName(getString(data[1]));
				result.setValue(getDouble(data[2]));
				return result;

			};
			results = list.stream().map(mapper).collect(Collectors.toList());
		}
		
		return results;
	}
	
	
	/**
	 * Obtiene los resultados de los factores de un modelo de clima
	 * @param evaluationId Identificador de evaluación
	 * @param isEnps Indica si se deben mostrar los resultados de las preguntas de clima o NPS
	 * @param divisionId Filtro para obtener resultados de factores por departamento
	 * 
	 */
	public List<ResultDTO> getFactorsByModelId(Long evaluationId, Boolean isEnps, Long divisionId, Long employeeId, String subsidiaries,
			Long companyId) {
		List<ResultDTO> results = new ArrayList<ResultDTO>();
		List<Object[]> list = new ArrayList<Object[]>();
		
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		
		String equation = "";		
		
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			if (evaluationNewModel.getModel() != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationNewModel.getModel().getId());
				if (configOpt.isPresent() && isEnps) {
					equation = getEquationTypeByConfiguration(configOpt.get());
				}
				list = climateRedshiftDao.getFactorsResultsByModel(evaluationNewModel.getId(), isEnps, companyId, 
						divisionId, subsidiaries, employeeId, equation);
			}
		} else {
			if (evaluationOldModel.getModel() != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationOldModel.getModel().getId());
				if (configOpt.isPresent() && isEnps) {
					equation = getEquationTypeByConfiguration(configOpt.get());
				}
				list = climateRedshiftDao.getFactorsResultsByModel(evaluationOldModel.getId(), isEnps, companyId,
						divisionId, subsidiaries, employeeId, equation);	
			}		
		}
		
		if (list != null) {
			results = processListResults(list);
		}
		
		return results;
	}
	
	
	
	/**
	 * Método que devuelve un listado de ResultDTO al procesar una lista de objetos
	 * 
	 * */
	private List<ResultDTO> processListResults(List<Object[]> list){
		
		List<ResultDTO> results = new ArrayList<ResultDTO>();	
			Function<Object[], ResultDTO> mapper = data -> {
				ResultDTO result = new ResultDTO();
				result.setId(getLong(data[0]));
				result.setName(getString(data[1]));
				result.setValue(new Double(data[3].toString()));
				return result;
			};
			results = list.stream().map(mapper).collect(Collectors.toList());
		return results;
	}
	
	
	
	/**
	 * Método que obtiene el promedio general de la compañía o de los colaboradores de un líder según los filtros
	 * @param evaluationId Identificador de la evaluación de clima
	 * @param employeeId Identificador del líder, puede ser null
	 * @return Double con el promedio
	 * 
	 * */
	public Double getAvgClimate(Long evaluationId, Long employeeId, Boolean isEnps, String subsidiary, Long companyId, List<Long> collaboratorIds) {

		Double avg = 0.0;
		String equation = "";

		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			if (evaluationNewModel != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationNewModel.getModel().getId());
				if (configOpt.isPresent() && isEnps) {
					equation = getEquationTypeByConfiguration(configOpt.get());
				}
				avg = climateRedshiftDao.getAvgClimateByEvaluationRedshift(evaluationId, companyId, 
						employeeId, isEnps, equation, subsidiary, collaboratorIds);
			}			
		} else {
			if (evaluationOldModel.getModel() != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationOldModel.getModel().getId());
				if (configOpt.isPresent() && isEnps) {
					equation = getEquationTypeByConfiguration(configOpt.get());					
				}
				avg = climateRedshiftDao.getAvgClimateByEvaluationRedshift(evaluationId, companyId, 
						employeeId, isEnps, equation, subsidiary, collaboratorIds);
			}
			
		}
		
		return avg;		
	}	

	/**
	 * Obtiene listado de dimensiones de un modelo de clima con su respectivo promedio  
	 * 
	 * @param evaluationId Identificador de evaluación
	 * @param isEnps Indica si pertenece a Clima (False) o NPS (True)
	 * @param divisionId Identificador del departamento
	 */
	public List<ResultDTO> getDimensionsByClimateEvaluation(Long evaluationId, Boolean isEnps, Long divisionId, Long employeeId, String subsidiaries,
			Long companyId) {
		
		List<ResultDTO> results = new ArrayList<ResultDTO>();
		List<Object[]> list = new ArrayList<Object[]>();
		String equation = "";
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			if (evaluationNewModel != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationNewModel.getModel().getId());
				if (configOpt.isPresent() && isEnps) {
					equation = getEquationTypeByConfiguration(configOpt.get());
				}
				list = climateRedshiftDao.getDimensionsResultsByModel(companyId, isEnps, divisionId, evaluationId, subsidiaries, employeeId, equation);
			}	
		} else {
			if (evaluationOldModel.getModel() != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationOldModel.getModel().getId());
				if (configOpt.isPresent() && isEnps) {
					equation = getEquationTypeByConfiguration(configOpt.get());
				}
				list = climateRedshiftDao.getDimensionsResultsByModel(companyId, isEnps, divisionId, evaluationId, subsidiaries, employeeId, equation);
			}			
		}
		
		if (list != null) {
			results = processListResults(list);
		}
		
		return results;
	}

	/**
	 * Obtiene los departamentos que tienen resultados para una evaluación de clima
	 * 
	 * @param companyId Identificador de la compañía
	 * @param evaluationId Identificador de una evaluación de clima
	 */
	public List<FilterDTO> getDivisionsByClimateEvaluation(Long companyId, Long evaluationId, Boolean isEnps) {
		
		List<FilterDTO> results = new ArrayList<FilterDTO>();
		Optional<Evaluation> evaluation = this.evaluationRepository.findById(evaluationId);
		if (evaluation.isPresent()) {
			List<Object[]> list = climateRedshiftDao.getDivisionsByClimateEvaluation(companyId, evaluation.get().getId(), isEnps);
			Function<Object[], FilterDTO> mapper = data -> {
				FilterDTO result = new FilterDTO();
				result.setId(getLong(data[0]));
				result.setName(getString(data[1]));
				return result;

			};
			results = list.stream().map(mapper).collect(Collectors.toList());
		}
		return results;
	}

	
	/**
	 * Obtiene los factores o dimensiones con el detalle de resultados y cantidad de preguntas, Puede ser por clima o enps
	 * 
	 * @param divisionId Identificador del departamento
	 * @param subsidiaryId Identificador de la sede
	 * @param isClimate Indica si se consultan los resultados de clima o enps
	 */
	@SuppressWarnings({ "unchecked" })
	public <T>PageableResponse<T> getFactorsOrDimensionsDetail(Long companyId, Long evaluationId, FiltersClimateDTO filters, Pageable pageable, 
			Boolean byDimension, Long employeeId) {
		
		List<QuestionDetailDTO> results = new ArrayList<QuestionDetailDTO>();
		
		
		PageableResponse<T> pages = new PageableResponse<T>();
		Integer maxResults = null;
		Integer startIndex = null;		
		if (pageable != null) {
			maxResults = pageable.getPageSize();
			startIndex = pageable.getPageSize() * pageable.getPageNumber();
		}
		String entityId= byDimension? "dimension_id":"factor_id";
		String entityName=byDimension? "dimension_name": "factor_name";
		
		List<Object[]> list = new ArrayList<Object[]>();
		
		Long modelId=0L;
				
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			   Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			   if (evaluationNewModel != null) {
	    		 modelId=evaluationNewModel.getModel().getId();
			   }
		}else {
			if (evaluationOldModel.getModel() != null) {
				modelId=evaluationOldModel.getModel().getId(); 
			}			 
		}
		
		if(modelId!=0L) {
			
			Optional<ClimateConfiguration> opt=climateConfigurationRepository.findByModelId(modelId);
			
			String equation="";
			
			if(opt.isPresent() && filters.getIsEnps()) {
			   equation=getEquationTypeByConfiguration(opt.get());
			}
			
			list = climateRedshiftDao.getFactorsOrDimensionsDetail(companyId, evaluationId, filters.getDivisionId(), filters.getGroupSubsidiaries(), entityId,
						entityName, filters.getIsEnps(), startIndex, maxResults, equation, employeeId);
			pages.setTotal(climateRedshiftDao.countFactorsOrDimensionsDetail(companyId, evaluationId, filters.getDivisionId(), filters.getGroupSubsidiaries(), 
					entityId, filters.getIsEnps(), employeeId));
		
			if(list.size()>0) {
				
					Function<Object[], QuestionDetailDTO> mapper = data -> {
						QuestionDetailDTO result = new QuestionDetailDTO();
						result.setId(getLong(data[0]));
						result.setName(getString(data[1]));
						result.setCountQuestions(((BigInteger) data[2]).intValue());
						result.setCountResponses(((BigDecimal) data[3]).intValue());
						result.setResult(new Double(data[5].toString()));
						return result;
		
					};
					results = list.stream().map(mapper).collect(Collectors.toList());
				
			}
		}
		
		pages.setElements((List<T>) results);
	
		return pages;
	}
	
	
	/**
	 * Obtiene las preguntas que pertenecen a una dimensión o factor
	 * 
	 * @param evaluationId Identificador de la evaluación
	 * @param filters Objeto con la información de los filtros por sede, departamento, si es enps, el identificador del factor o la dimensión etc
	 * @return List<ResultDTO> Listado de preguntas
	 */
	public List<CategoryDTO> getQuestionsByDimensionOrFactor(Long companyId, Long evaluationId, FiltersClimateDTO filters, Long employeeId) {
		
		List<CategoryDTO> results = new ArrayList<CategoryDTO>();
		List<Object[]> list =new ArrayList<Object[]>();
		
		Long modelId=0L;
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			if (evaluationNewModel != null) {
				modelId=evaluationNewModel.getModel().getId();
			}
		}else {
			if(evaluationOldModel.getModel()!=null) {
			  modelId=evaluationOldModel.getModel().getId();	
			}
		}
		
		
		if(modelId>0L) {
			Optional<ClimateConfiguration> opt=climateConfigurationRepository.findByModelId(modelId);
			String equation="";
			if(opt.isPresent() && filters.getIsEnps()) {
				equation=getEquationTypeByConfiguration(opt.get());
			}
			list = climateRedshiftDao.getQuestionsDetailByDimensionOrFactor(companyId, evaluationId, filters.getDivisionId(), filters.getGroupSubsidiaries(), 
					filters.getIsByDimension(), filters.getEntityId(),  filters.getIsEnps(), equation, employeeId);

		}	
		
		Function<Object[], CategoryDTO> mapper = data -> {
				   CategoryDTO  question = new CategoryDTO();
					question.setId(getLong(data[0]));
					question.setName(getString(data[1]));
					question.setValue(new Double(data[3].toString()));
					question.setResults(getOptionsResponsesByQuestion(companyId, evaluationId, question.getId(), filters));
					return question;
	
		};
		
		results = list.stream().map(mapper).collect(Collectors.toList());
		
		return results;
	}
	
	
	private List<ResultDTO> getOptionsResponsesByQuestion(Long companyId, Long evaluationId, Long questionId, FiltersClimateDTO filters ) {
		
		List<Object[]> list = climateRedshiftDao.getSatisfactionResultsByEvaluationClimate(companyId, evaluationId,questionId, filters.getIsEnps(), filters.getGroupSubsidiaries());
		
		Function<Object[], ResultDTO> mapper = data -> {
			   ResultDTO  option = new ResultDTO();
				option.setId(getLong(data[0]));
				option.setName(getString(data[1]));
				option.setValue(new Double(data[2].toString()));
				return option;

		};
		
		return list.stream().map(mapper).collect(Collectors.toList());
		
		
	}
	
	
	/**
	 * Obtiene listado de los departamentos con su respectivo promedio  
	 * 
	 * @param evaluationId Identificador de evaluación
	 * @param isEnps Indica si pertenece a Clima (false) o NPS (true)
	 */
	public List<ResultDTO> getDivisionsWithResultsByClimateEvaluation(Long evaluationId, Boolean isEnps) {
		
		List<ResultDTO> results = new ArrayList<ResultDTO>();
		List<Object[]> list = new ArrayList<Object[]>();
		String equation = "";
		
		
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			if (evaluationNewModel != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationNewModel.getModel().getId());
				if (configOpt.isPresent()) {
					equation = getEquationTypeByConfiguration(configOpt.get());
				}
				list = climateRedshiftDao.getDivisionsWithResultsByClimateEvaluation(evaluationNewModel.getCompany().getId(), 
						evaluationId, isEnps, equation);
			}	
		} else {
			if (evaluationOldModel.getModel() != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationOldModel.getModel().getId());
				if (configOpt.isPresent() && isEnps) {
					equation = getEquationTypeByConfiguration(configOpt.get());
				}
				list = climateRedshiftDao.getDivisionsWithResultsByClimateEvaluation(evaluationOldModel.getCompany().getId(), evaluationId, isEnps, equation);
			}				
		}
		
		if (list != null) {
			results = processListResults(list);
		}
		return results;
	}

	
	/**
	 * Obtiene las sedes que tienen resultados para una evaluación de clima
	 * 
	 * @param companyId Identificador de la compañía
	 * @param evaluationId Identificador de una evaluación de clima
	 */
	public List<FilterDTO> getSubsidiariesByClimateEvaluation(Long companyId, Long evaluationId, Boolean isEnps) {
		
		List<FilterDTO> results = new ArrayList<FilterDTO>();
		Optional<Evaluation> evaluation = this.evaluationRepository.findById(evaluationId);
		if (evaluation.isPresent()) {
			List<Object[]> list = climateRedshiftDao.getSubsidiariesByClimateEvaluation(companyId, evaluation.get().getId(), isEnps);
			Function<Object[], FilterDTO> mapper = data -> {
				FilterDTO result = new FilterDTO();
				result.setId(getLong(data[0]));
				result.setName(getString(data[1]));
				return result;

			};
			results = list.stream().map(mapper).collect(Collectors.toList());
		}
		return results;
	}

	/**
	 * Obtiene los resultados de los factores o dimensiones que son comunes entre 2
	 * evaluaciones de clima
	 * 
	 * @param typeFilter Tipo de Filtro: DIMENSION o FACTOR
	 * @param filters    Filtros para los resultados
	 */
	public CompareResultsDTO getGroupedResultsBetweenClimateEvaluations(Long companyId, String typeFilter, FiltersCompareClimateDTO filters) {
		
		CompareResultsDTO finalResults = new CompareResultsDTO();
		List<ResultDTO> primaryResults = new ArrayList<ResultDTO>();
		List<ResultDTO> secondaryResults = new ArrayList<ResultDTO>();
		
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(filters.getEvaluationIdPrimary(), true);
		String equation = "";
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(filters.getEvaluationIdPrimary());
			if (evaluationNewModel.getModel() != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationNewModel.getModel().getId());
				if (configOpt.isPresent() && filters.getIsEnps()) {
					equation = getEquationTypeByConfiguration(configOpt.get());
				}
			}			
		} else {
			if (evaluationOldModel.getModel() != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationOldModel.getModel().getId());
				if (configOpt.isPresent() && filters.getIsEnps()) {
					equation = getEquationTypeByConfiguration(configOpt.get());
				}
			}			
		}
		
		if (typeFilter.equals("DIMENSION")) {
			//Obtiene resultados dimensiones evaluación principal
			primaryResults = processListResults(climateRedshiftDao.getDimensionsResultsByModel(companyId, filters.getIsEnps(),
					filters.getDivisionId(), filters.getEvaluationIdPrimary(), filters.getGroupSubsidiaries(), null, equation));
			finalResults.setPrimaryResults(primaryResults);
			
			if (filters.getEvaluationIdSecondary() != null) {
				//Obtiene resultados dimensiones evaluación secundaria
				secondaryResults = processListResults(climateRedshiftDao.getDimensionsResultsByModel(companyId, filters.getIsEnps(),
						filters.getDivisionId(), filters.getEvaluationIdSecondary(), filters.getGroupSubsidiaries(), null, equation));	
				finalResults.setSecondaryResults(secondaryResults);
				//Compara resultados comunes entre las 2 evaluaciones
				finalResults = compareCommonResults(finalResults, primaryResults, secondaryResults);
			}	
		} else if (typeFilter.equals("FACTOR")) {
			//Obtiene resultados factores evaluación principal
			primaryResults = processListResults(climateRedshiftDao.getFactorsResultsByModel(filters.getEvaluationIdPrimary(), filters.getIsEnps(),
					companyId, filters.getDivisionId(), filters.getGroupSubsidiaries(), null, equation));
			finalResults.setPrimaryResults(primaryResults);
			
			if (filters.getEvaluationIdSecondary() != null) {
				//Obtiene resultados factores evaluación secundaria
				secondaryResults = processListResults(climateRedshiftDao.getFactorsResultsByModel(filters.getEvaluationIdSecondary(), filters.getIsEnps(),
						companyId, filters.getDivisionId(), filters.getGroupSubsidiaries(), null, equation));			
				finalResults.setSecondaryResults(secondaryResults);
				//Compara resultados comunes entre las 2 evaluaciones
				finalResults = compareCommonResults(finalResults, primaryResults, secondaryResults);
			}	
		}
		return finalResults;
	}
	
	/**
	 * Método encargado de encontrar los elementos que tienen el mismo nombre entre los resultados primarios y secundarios
	 */
	private CompareResultsDTO compareCommonResults(CompareResultsDTO finalResults, List<ResultDTO> primaryResults, List<ResultDTO> secondaryResults) {
		List<String> nameResults = new ArrayList<String>();
		primaryResults.stream().forEach(firstResult -> {
			nameResults.add(firstResult.getName());
		});
		secondaryResults.stream().forEach(secondResult -> {
			nameResults.add(secondResult.getName());
		});
		final List<String> commonResults = nameResults.stream().distinct().filter(i -> Collections.frequency(nameResults, i) > 1).collect(Collectors.toList());
		finalResults.setPrimaryResults(primaryResults.stream().filter(r->commonResults.contains(r.getName())).collect(Collectors.toList()));
		finalResults.setSecondaryResults(secondaryResults.stream().filter(r->commonResults.contains(r.getName())).collect(Collectors.toList())); 
		return finalResults;
	}
	
	
	/**
	 * Metodo que permite obtener los limites, colores y las etiquetas del semaforo
	 * de la evaluacion de clima
	 * 
	 * @param evaluationId Identificador de la evaluación de clima
	 * @return Listado de DTO del semaforo
	 */
	public List<SemaphoreDTO> getSemaphore(Long evaluationId, Boolean isEnps) {
	
		List<SemaphoreDTO> semaphoreEvaluation = new ArrayList<SemaphoreDTO>();
		
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		Climate evaluation =null;
		if (evaluationOldModel== null) {
			evaluation= this.climateEvaluationRepository.findEvaluationById(evaluationId);
		}
		
		if((evaluation!=null && evaluation.getModel()!=null ) || (evaluationOldModel!=null && evaluationOldModel.getModel()!=null)) {
			
			Long modelId=evaluation!=null? evaluation.getModel().getId():evaluationOldModel.getModel().getId();
			Optional<ClimateConfiguration> configurationOpt=climateConfigurationRepository.findByModelId(modelId);
			
			if(configurationOpt.isPresent()) {
				
				ClimateConfiguration confi=configurationOpt.get();
				// COLOR ROJO
				if ((!isEnps && confi.getRedLimit() != null) || (isEnps && confi.getDetractorMaxLimit()!=null)) {
					String labelRed=isEnps?confi.getDetractorLabel() : confi.getRedLabel();
					Double limitRed=isEnps? ((confi.getDetractorMaxLimit()*100)/confi.getPromoterMaxLimit()) : confi.getRedLimit();
					addSemaphore(semaphoreEvaluation, limitRed, labelRed, "#E55C75");
				}
				
				// COLOR NARANJA
				if (confi.getOrangeLimit() != null  && !isEnps) {
					addSemaphore(semaphoreEvaluation, confi.getOrangeLimit(), confi.getOrangeLabel(), "#FA9F47");
				}
				
				// COLOR AMARILLO
				if ((!isEnps && confi.getYellowLimit() != null) || (isEnps && confi.getNeutralMaxLimit()!=null)) {
					String labelY=isEnps?confi.getNeutralLabel() : confi.getYellowLabel();
					Double limitY=isEnps? ((confi.getNeutralMaxLimit()*100)/confi.getPromoterMaxLimit()) : confi.getYellowLimit();
					addSemaphore(semaphoreEvaluation, limitY, labelY, "#FFCB55");
				}
				
				// COLOR LIMA
				if (confi.getLimeLimit() != null  && !isEnps) {
					addSemaphore(semaphoreEvaluation, confi.getLimeLimit(), confi.getLimeLabel(), "#7BD67E");
				}
				//COLOR VERDE
				if ((!isEnps && confi.getGreenLimit() != null) || (isEnps && confi.getPromoterMaxLimit()!=null)) {
					String labelGreen=isEnps?confi.getPromoterLabel() : confi.getGreenLabel();
					Double limitGreen=isEnps? ((confi.getPromoterMaxLimit()*100)/confi.getPromoterMaxLimit()) : confi.getGreenLimit();
					addSemaphore(semaphoreEvaluation, limitGreen, labelGreen, "#16B382");
				}
				
			}
		
		}
	
		return semaphoreEvaluation;
	
	}
	
	/**
	 * Agrega un intervalo con información del semáforo a la lista de todo el semaforo completo
	 */
	private void addSemaphore(List<SemaphoreDTO> list, Double limit, String label, String colorCode) {

		SemaphoreDTO dataCompOne = new SemaphoreDTO();
		// se setea el valor del limite al intervalo
		dataCompOne.setCompLimit(limit);
		// se setea el label del intervalo
		dataCompOne.setLabel(label);
		dataCompOne.setColor(colorCode);
		
		// se agrega a la lista de todos los limites
		list.add(dataCompOne);

	}
		
	/**
	 * Método que ejecuta procedimiento almacenado para volver a calcular resultados de un modelo de clima
	 * @param companyId Identificador de la compañía
	 * @param evaluationId Identificador de la evaluación de clima
	 */
	public void executeStoredProcedureClimate(Long companyId, Long evaluationId) {
		
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			if (evaluationNewModel != null) {				
				climateRedshiftDao.executeStoredProcedureClimate(companyId, evaluationNewModel.getModel().getId(), evaluationId);
			}
		} else {
			climateRedshiftDao.executeStoredProcedureClimate(companyId, evaluationOldModel.getModel().getId(), evaluationId);
		}
		
	}
	
	
	/**
	 * Obtiene los departamentos con sus factores o dimensiones y respectivos promedios
	 * 
	 * @param companyId Identificador de la empresa
     * @param evaluationId Identificador de la evaluación
	 * @param filters Identificador de la sede, si es enps, y si la consulta es por dimensión
	 * @param isClimate Indica si se consultan los resultados de clima o enps
	 */
	@SuppressWarnings({ "unchecked" })
	public <T>PageableResponse<T> getDivisionsWithFactorsOrDimensionsAvg(Long companyId, Long evaluationId, FiltersClimateDTO filters, Pageable pageable) {
		
		List<CategoryDTO> results = new ArrayList<CategoryDTO>();
		
		PageableResponse<T> pages = new PageableResponse<T>();
		Integer maxResults = null;
		Integer startIndex = null;		
		if (pageable != null) {
			maxResults = pageable.getPageSize();
			startIndex = pageable.getPageSize() * pageable.getPageNumber();
		}
		String entityId= filters.getIsByDimension()? "dimension_id":"factor_id";
		String entityName=filters.getIsByDimension()? "dimension_name": "factor_name";
		
		List<Object[]> list = new ArrayList<Object[]>();
		String equation = "";
				
		//Se consulta evaluación según id en modelo antiguo y si no existe se consulta en modelo nuevo
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			   Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			   if (evaluationNewModel != null) {
					if (evaluationNewModel.getModel() != null) {
						Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationNewModel.getModel().getId());
						if (configOpt.isPresent() && filters.getIsEnps()) {
							equation = getEquationTypeByConfiguration(configOpt.get());
						}
					   //Primero consultamos los departamentos paginados y despues consultamos los factores o dimensiones de esos dptos
						List<Object[]> divisions=climateRedshiftDao.getDivisionsWithResultsPaginatedAndFilters(evaluationNewModel.getCompany().getId(), evaluationNewModel.getId(), filters.getIsEnps(),
								filters.getGroupSubsidiaries(),  startIndex, maxResults);
						List<Long> ids= divisions.stream().map(obj-> (getLong(obj[0]))).collect(Collectors.toList());
						if(ids.size()>0) {
				    		list= climateRedshiftDao.getDivisionsWithDimensionOrFactorAvg(evaluationNewModel.getCompany().getId(), evaluationNewModel.getId(), 
				    				filters.getGroupSubsidiaries(), entityId, entityName, filters.getIsEnps(), ids, equation);
						}
					}					
	    	   }
		}else {
			if (evaluationOldModel.getModel() != null) {
				Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationOldModel.getModel().getId());
				  if (configOpt.isPresent() && filters.getIsEnps()) {
					  equation = getEquationTypeByConfiguration(configOpt.get());
				  }
				  List<Object[]> divisions=climateRedshiftDao.getDivisionsWithResultsPaginatedAndFilters(evaluationOldModel.getCompany().getId(), evaluationOldModel.getId(), filters.getIsEnps(),
						filters.getGroupSubsidiaries(),  startIndex, maxResults);
				   List<Long> ids= divisions.stream().map(obj-> (getLong(obj[0]))).collect(Collectors.toList());
				   if(ids.size()>0) {
					   list = climateRedshiftDao.getDivisionsWithDimensionOrFactorAvg(evaluationOldModel.getCompany().getId(), evaluationOldModel.getId(), 
							   filters.getGroupSubsidiaries(), entityId, entityName, filters.getIsEnps(), ids, equation);
				   }
			}
		}
			

	    if(list.size()>0) {
	    	list.stream().forEach(data -> {
			   Optional<CategoryDTO> optDivision =results.stream()
					.filter(d -> d.getId() == ((BigInteger) data[0]).longValue()).findFirst();
					
					if (!optDivision.isPresent()) {
						CategoryDTO div = new CategoryDTO();
						div.setId(getLong(data[0]));
						div.setName((String) data[1]);
						List<ResultDTO> listResults = new ArrayList<ResultDTO>();
						ResultDTO entity=new ResultDTO();
						entity.setId(getLong(data[2]));
						entity.setName(getString(data[3]));
						entity.setValue(new Double(data[5].toString()));
						listResults.add(entity);
						div.setResults(listResults);
						results.add(div);
					} else {
						Integer index=results.indexOf(optDivision.get());
						ResultDTO entity=new ResultDTO();
						entity.setId(getLong(data[2]));
						entity.setName(getString(data[3]));
						entity.setValue(new Double(data[5].toString()));
						results.get(index).getResults().add(entity);							
					}
	    	 });
		 }
		pages.setElements((List<T>) results);
		pages.setTotal(climateRedshiftDao.getDivisionsWithResultsPaginatedAndFilters(companyId, evaluationId, filters.getIsEnps(),
				filters.getGroupSubsidiaries(),  null,null).size());
	
		return pages;
	}
	
	
	
	/**
	 * Método que devuelve que tipo de preguntas tiene la evaluación: Solo de clima, enps o ambas
	 * @param companyId Identificador de la empresa
	 * @param evaluationId Identificador de la evaluación
	 * @return String con las opciones:CLIMATE, ENPS o BOTH
	 * */
	public String getTypesEvaluationClimate(Long companyId, Long evaluationId) {
		
		String type="";
		List<String> list=new ArrayList<String>();
		
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
			if(evaluationNewModel!=null && evaluationNewModel.getModel()!=null) {
				list=climateRedshiftDao.getTypesEvaluationOfModelId(companyId, evaluationNewModel.getModel().getId());
			}
		}else if (evaluationOldModel.getModel()!=null) {
			    list=climateRedshiftDao.getTypesEvaluationOfModelId(companyId, evaluationOldModel.getModel().getId());
		}

		if(list.size()==1) {
			if((list.get(0)).equals("enps")){
				type="ENPS";
			}else {
				type="CLIMATE";
			}
		}else if(list.size()==2) {
			type="BOTH";
		}
		
		return type;
		
	}
	
	
	/**
	 * Método que devuelve un  resultado  con el porcentaje de promotores, neutros y detractores
	 * @param companyId Identificador de la empresa
	 * @param evaluationId Identificador de la evaluación
	 * @param subsidiaries 
	 * @return lista con el porcentaje de promotores, neutros y detractores
	 * 
	 * */
	public List<ResultDTO> getResultEnpsGroupByTypes(Long companyId, Long evaluationId, String subsidiaries) {
		

		Object[] list= new Object[5];
		List<ResultDTO> dto=new ArrayList<ResultDTO>();
		ClimateConfiguration config= new ClimateConfiguration();
		Long modelId=null;
		
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId); 
			if(evaluationNewModel!=null && evaluationNewModel.getModel()!=null) {
			   modelId=evaluationNewModel.getModel().getId();
			}
		}else if (evaluationOldModel.getModel()!=null) {
			  modelId=evaluationOldModel.getModel().getId();
		}
		
		if(modelId!=null) {
			
			Optional<ClimateConfiguration> configOpt=climateConfigurationRepository.findByModelId(modelId);
			
			if(configOpt.isPresent() && configOpt.get().getCalculationType()!=null) {
			 config=configOpt.get();
			 String type=getEquationTypeByConfiguration(config);
			 list=climateRedshiftDao.getResultsOptionsGeneralEnps(companyId,evaluationId, type, subsidiaries);
			 
				if(list!=null && config!=null) {
					ResultDTO promoters=createResultDTOEnps(1L, config.getPromoterLabel(), list, 3);
					dto.add(promoters);
					
					ResultDTO neutral=createResultDTOEnps(2L, config.getNeutralLabel(), list, 2);
					dto.add(neutral);
					
					ResultDTO detractors=createResultDTOEnps(3L, config.getDetractorLabel(), list, 1);
					dto.add(detractors);

				}
			 
			}
		}
		
		
		return dto;
		
	}
	
	/**
	 * Método que devuelve un texto con la ecuación a aplicar en la consulta según el tipo de cálculo de enps escogido
	 * @param ClimateConfiguration Información de la configuración
	 * */
	private String getEquationTypeByConfiguration(ClimateConfiguration config) {
		
		String equation="";
		if(config.getCalculationType()!=null) {
			equation=config.getCalculationType().getEquation();
		}
		
		return equation;
		
	}
	
	
	/**
	 * Método que crea el resultDto para detractores, promotores y neutros
	 * */
	private ResultDTO createResultDTOEnps(Long id, String label, Object[] list, int positionColumn ) {
		
		
		ResultDTO type=new ResultDTO();
		type.setId(id);
		type.setName(label);
		Double value=0.0;
		if(getDouble(list[0])>0) {
			value=(double) ((((new Double(list[positionColumn].toString()))) *100)/(new Double(list[0].toString())));
		}
		type.setValue(value);
		
		return type;
		
	}
	
	
	/**
	 * Obtiene las palabras clave extraídas de los comentarios hechos en preguntas abiertas de una evaluación
	 * @param evaluationId Identificador evaluación de clima
	 * @param subsidiaries 
	 */
	@Transactional
	public List<KeywordClimateDTO> getClimateKeywordsByEvaluation(Long evaluationId, String subsidiaries) {
		
		List<KeywordClimateDTO> keyWordList = new ArrayList<KeywordClimateDTO>();
		

			Long modelId = null;
			// Se consulta modelo de la evaluación
			FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
			if (evaluationOldModel == null) {
				Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId); 
				if(evaluationNewModel!=null && evaluationNewModel.getModel()!=null) {
				   modelId = evaluationNewModel.getModel().getId();
				}
			} else if (evaluationOldModel.getModel() != null) {
				modelId = evaluationOldModel.getModel().getId();
			}
			
			Optional<Long> optModel = Optional.of(modelId);
			List<Object[]> optList = climateResultsDao.findByModelIdAndResponseContaining(optModel.get(), evaluationId, subsidiaries);
			
			if(optList.size()>0) {
				//Long totalWords = ((BigDecimal) optList.get().get(0)[4]).longValue();
				for (Object[] keyword : optList) {
					KeywordClimateDTO  dto = new KeywordClimateDTO();
					dto.setWord(keyword[0].toString());
					int total=((BigDecimal) keyword[3]).intValue();
					dto.setType(keyword[2].toString());
					dto.setTotalCount(((BigInteger) keyword[1]).intValue());
					dto.setPercentage((double)(dto.getTotalCount()*100)/total);
					
					
					
					int totalCount = (((BigInteger) keyword[4]).intValue());
					int countPositive = (((BigInteger) keyword[5]).intValue());
					dto.setPositivePercentage((double)(countPositive*100/totalCount));
					int countNeutral = (((BigInteger) keyword[6]).intValue());
					dto.setNeutralPercentage((double)(countNeutral*100/totalCount));
					int countMixed = (((BigInteger) keyword[7]).intValue());
					dto.setMixedPercentage((double)(countMixed*100/totalCount));
					int countNegative = (((BigInteger) keyword[8]).intValue());
					dto.setNegativePercentage((double)(countNegative*100/totalCount));
					
					keyWordList.add(dto);
				}
			}
			
		
		
		return keyWordList;
	}
	
	
	
	/**
	 * Método que obtiene las preguntas abiertas de una evaluación y puede filtrarse por una palabra en las respuestas
	 * @param evaluationId Identificador evaluación de clima
	 * @param word Palabra a filtrar en las preguntas abiertas
	 * @param subsidiaries Ids de las sedes 
	 * 
	 * */
	public List<FilterDTO> getClimateModelQuestionDTOByModelId(Long evaluationId, String word, String subsidiaries) {
		List<FilterDTO> questions = new ArrayList<FilterDTO>();

		ClimateModel model=null;
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId); 
			if(evaluationNewModel!=null && evaluationNewModel.getModel()!=null) {
			   model=evaluationNewModel.getModel();
			}
		}else if (evaluationOldModel.getModel()!=null) {
			  model=evaluationOldModel.getModel();
		}
		
		if (model != null) {
		 List<Object[]>	mQuestions = climateResultsDao.getOpenQuestionByFilter(evaluationId,model.getId(), word, subsidiaries);
		 
			if (mQuestions != null && !mQuestions.isEmpty()) {

				Function<Object[], FilterDTO> questionMapper = mQuestion -> {
					FilterDTO question = new FilterDTO();
					question.setId(getLong(mQuestion[0]));
					question.setName(getString(mQuestion[1]));
					return question;
				};

				questions = mQuestions.stream().map(questionMapper).collect(Collectors.toList());

			}
		}

		return questions;
	}
	
	
	
	/**
	 * Método que obtiene los resultados agrupados por sentimiento
	 * @param evaluationId Identificador evaluación de clima
	 * @param word Palabra a filtrar en las preguntas abiertas
	 * @param questionId Identificador de la pregunta abierta a filtrar
	 * @param divisionId Identificador del departamento a filtrar
	 * @param subsidiaries 
	 * 
	 * */
	public List<ResponseDTO> getResultsSentiments(Long evaluationId, String word, Long questionId, Long divisionId, String subsidiaries){
		
		List<ResponseDTO> sentiments=new ArrayList<ResponseDTO>();
		
		ClimateModel model=null;
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId); 
			if(evaluationNewModel!=null && evaluationNewModel.getModel()!=null) {
			   model=evaluationNewModel.getModel();
			}
		}else if (evaluationOldModel.getModel()!=null) {
			  model=evaluationOldModel.getModel();
		}
		
		if (model != null) {
			List<Object[]> list=climateResultsDao.getSentimentsByModel(evaluationId, model.getId(), word, questionId, divisionId, subsidiaries);
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
			
		}
		
		return sentiments;
	
	}
	
	
	
	/**
	 * Método que obtiene los departamentos con preguntas abiertas según los filtros dados
	 * @param evaluationId Identificador evaluación de clima
	 * @param word Palabra a filtrar en las preguntas abiertas
	 * @param questionId Identificador de la pregunta abierta a filtrar
	 * @param subsidiaries Identificador de las sedes
	 * 
	 * */
	public List<FilterDTO> getDivisionsByOpenQuestions(Long evaluationId, String word, Long questionId, String subsidiaries) {
		List<FilterDTO> questions = new ArrayList<FilterDTO>();

		ClimateModel model=null;
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId); 
			if(evaluationNewModel!=null && evaluationNewModel.getModel()!=null) {
			   model=evaluationNewModel.getModel();
			}
		}else if (evaluationOldModel.getModel()!=null) {
			  model=evaluationOldModel.getModel();
		}
		
		if (model != null) {
		 List<Object[]>	divisions = climateResultsDao.getDivisionsByOpenQuestions(evaluationId,model.getId(), word, questionId, subsidiaries);
		 
			if (divisions != null && !divisions.isEmpty()) {

				Function<Object[], FilterDTO> questionMapper = mQuestion -> {
					FilterDTO question = new FilterDTO();
					question.setId(getLong(mQuestion[0]));
					question.setName(getString(mQuestion[1]));
					return question;
				};

				questions = divisions.stream().map(questionMapper).collect(Collectors.toList());

			}
		}

		return questions;
	}

	/**
	 * Obtiene los resultados de preguntas sociodemográficas de una evaluación
	 * @param evaluationId Identificador evaluación de clima
	 * @param subsidiary 
	 */
	public List<SociodemographicDTO> getSociodemographicResultsByEvaluation(Long evaluationId, String subsidiaries) {
		
		List<SociodemographicDTO> sociodemographicResults = new ArrayList<SociodemographicDTO>();
		//Se consultan preguntas sociodemográficas de la evaluación que tienen respuestas
		Optional<List<ClimateDemographicField>> sociodemographicList = climateDemographicFieldRepository.getClimateDemographicFieldByEvaluationId(evaluationId);
		if (sociodemographicList.isPresent()) {
			sociodemographicList.get().stream().forEach(field -> {
				SociodemographicDTO resultDTO = new SociodemographicDTO();
				
				resultDTO.setId(field.getId());
				resultDTO.setName(field.getName());
				List<ResponseDTO> results = new  ArrayList<ResponseDTO>();
				// Se obtienen opciones de respuesta con sus resultados
				List<ClimateDemographicOption> options = climateDemographicOptionRepository.getClimateDemographicOptionListByField(field);
				
				List<List<Long>> counts = new ArrayList<>(climateResultsDao.countByFieldId(evaluationId, field.getId(), subsidiaries));
				
				Map<Long,Long> map = counts.stream().collect(Collectors.toMap(item -> item.get(1), item -> item.get(2)));
				
				int totalCount = counts.stream().mapToInt(count -> count.get(2).intValue()).sum();
			
				options.stream().forEach(opt -> {
					ResponseDTO response = new ResponseDTO();
					response.setLabel(opt.getKey());
					Long countOption = map.get(opt.getId()) == null ? 0L : map.get(opt.getId());
					response.setCountResponses(countOption.intValue());
					response.setPercentage((double) (countOption*100.00/totalCount));
					results.add(response);
				});
				resultDTO.setResults(results);
				sociodemographicResults.add(resultDTO);
			});
		}		
		
		return sociodemographicResults;
	}
	
	
	/**
	 * Método que obtiene las respuestas abiertas de una evaluación y un modelo
	 * @param evaluationId Identificador de la evaluación
	 * @param word Palabra a filtrar en las preguntas abiertas
	 * @param questionId Identificador de la pregunta abierta a filtrar
	 * @param divisionId Identificador del departamento a filtrar
	 * @param subsidiaries Identificador de las sedes
	 * */
	public List<OpenQuestionResponseDTO> getOpenResponsesByEvaluation(Long evaluationId, String word, Long questionId, Long divisionId, String subsidiaries){
		
		List<OpenQuestionResponseDTO> responses=new ArrayList<OpenQuestionResponseDTO>();
		
		ClimateModel model=null;
		FormEvaluation evaluationOldModel = this.evaluationRepository.getOneFormEvaluationByIdAndClimate(evaluationId, true);
		if (evaluationOldModel == null) {
			Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId); 
			if(evaluationNewModel!=null && evaluationNewModel.getModel()!=null) {
			   model=evaluationNewModel.getModel();
			}
		}else if (evaluationOldModel.getModel()!=null) {
			  model=evaluationOldModel.getModel();
		}
		
		if (model != null) {
			
			List<Object[]> list=climateResultsDao.getOpenResponses(evaluationId, model.getId(), word, questionId, divisionId, subsidiaries);
		    list.stream().forEach(response->{
		    	OpenQuestionResponseDTO dto=new OpenQuestionResponseDTO();
		    	dto.setId(getLong(response[0]));
		    	dto.setResponse(getString(response[1]));
		    	dto.setSentimentType(SentimentType.valueOf(getString(response[2])));
		    	dto.setQuestionId(getLong(response[3]));
		    	responses.add(dto);
		    });
		}
		
		
		return responses;
		
	}

	/**
	 * Método que obtiene todos los datos para generar la sábana de datos
	 * 
	 * @param companyId    Identificador de la compañia
	 * @param evaluationId Identificador de la evaluación
	 * @param filters      filtros con los parametros para generar los datos
	 * @param isEnps       Indica si muestra resultados de clima o enps
	 * @param subsidiaries   Sede(s) de un rol subadmin
	 */
	public byte[] getClimateExcelReport(Long companyId, Long evaluationId, ClimateExcelFiltersDTO filters,
			Boolean isEnps, String subsidiaries) {
		
		// Se combinan sedes de rol subadmin y de filtro de componentes
		String subsidiariesQuestion = null;
		String subsidiariesHeatMap = null;
		if (subsidiaries != null) {
			if (filters.getSubsidiaryIdQuestion() != null) {
				subsidiariesQuestion = subsidiaries + "," +filters.getSubsidiaryIdQuestion();				
			} else {
				subsidiariesQuestion = subsidiaries;
			}
			if (filters.getSubsidiaryIdHeatMap() != null) {
				subsidiariesHeatMap = subsidiaries + "," +filters.getSubsidiaryIdHeatMap();			
			} else {
				subsidiariesHeatMap = subsidiaries;
			}
		} else  {
			if (filters.getSubsidiaryIdQuestion() != null) subsidiariesQuestion = filters.getSubsidiaryIdQuestion().toString();
			if (filters.getSubsidiaryIdHeatMap() != null) subsidiariesHeatMap = filters.getSubsidiaryIdHeatMap().toString();
		}
		

		Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
		// Se consultan resultados de satisfacción
		List<Object[]> satisfactionResults = climateRedshiftDao.getSatisfactionResultsByEvaluationClimate(companyId,
				evaluationId, null, isEnps, subsidiaries);
		List<Object[]> resultsByDimension = null;
		List<Object[]> resultsByFactor = null;
		List<Object[]> questionResults = null;
		List<Object[]> openResponses = null;
		List<Object[]> sociodemographicResults = null;
		
		if (evaluationNewModel != null) {
			Optional<ClimateConfiguration> configOpt = climateConfigurationRepository.findByModelId(evaluationNewModel.getModel().getId());
			String equation = "";
			if (configOpt.isPresent() && isEnps) {
				equation = getEquationTypeByConfiguration(configOpt.get());
			}
			// Se obtienen los departamentos con resultados de la empresa y posteriormente sus dimensiones y factores
			List<Object[]> divisions = climateRedshiftDao.getDivisionsByClimateEvaluation(companyId, evaluationId,
					isEnps);
			List<Long> divisionIds = divisions.stream().map(obj -> (getLong(obj[0]))).collect(Collectors.toList());
			if (divisionIds.size() > 0) {
				if (filters.getIsByDimension() == null || filters.getIsByDimension()) {
					resultsByDimension = climateRedshiftDao.getDivisionsWithDimensionOrFactorAvg(companyId,
							evaluationId, subsidiariesHeatMap, "dimension_id", "dimension_name", isEnps, divisionIds, equation);
				}

				if (filters.getIsByDimension() == null || !filters.getIsByDimension()) {
					resultsByFactor = climateRedshiftDao.getDivisionsWithDimensionOrFactorAvg(companyId, evaluationId,
							subsidiariesHeatMap, "factor_id", "factor_name", isEnps, divisionIds, equation);
				}
			}
			
			questionResults = climateRedshiftDao.getQuestionsDetailExcel(companyId, evaluationId, filters.getDivisionIdQuestion(), 
					subsidiariesQuestion, isEnps, equation);
			
			openResponses = climateResultsDao.getOpenResponsesExcel(evaluationId, evaluationNewModel.getModel().getId(), 
					filters.getWordOpen(), filters.getQuestionId(), filters.getDivisionIdOpen(), subsidiaries);
			
			sociodemographicResults = climateResultsDao.getSociodemographicExcel(evaluationNewModel.getModel().getId());
			// Calcula el porcentaje de respuesta de cada opción
			if (sociodemographicResults != null && sociodemographicResults.size() > 0) {
				sociodemographicResults = climateResultsDao.getSociodemographicExcel(evaluationNewModel.getModel().getId());
				sociodemographicResults.stream().forEach(sd -> {
					int totalCount = climateResultsDao.getTotalCountByFieldId(evaluationNewModel.getModel().getId(), 
							((BigInteger) sd[0]).longValue(), subsidiaries);
					sd[3] = ((BigInteger) sd[3]).longValue()*100.00/totalCount;
				});				
			}
		}		

		return excelHandler.getClimateExcelReport(satisfactionResults, resultsByDimension, resultsByFactor,
				questionResults, openResponses, sociodemographicResults, companyId);
	}

	/**
	 * Método que obtiene todos los datos para generar la sábana de datos
	 * 
	 * @param companyId    Identificador de la compañia
	 * @param evaluationId Identificador de la evaluación
	 * @param isEnps       Indica si muestra resultados de clima o enps
	 * @param employeeId   Identificador de empleado (líder)
	 */
	public byte[] getClimateLeaderExcelReport(Long companyId, Long evaluationId, Boolean isEnps, Long employeeId) {
		
		List<Object[]> resultsByDimension = null;
		List<Object[]> resultsByFactor = null;
		List<Object[]> questionResults = null;

		// Valida existencia de la evaluación
		Boolean isPresentEvaluation = false;
		Climate evaluationNewModel = this.climateEvaluationRepository.findEvaluationById(evaluationId);
		if (evaluationNewModel == null) {
			Optional<FormEvaluation> evaluationOldModel = this.evaluationRepository.findEvaluationById(evaluationId);
			if (evaluationOldModel.isPresent()) {
				isPresentEvaluation = true;
			}
		} else {
			isPresentEvaluation = true;
		}

		if (isPresentEvaluation) {
			Optional<ClimateConfiguration> configOpt = Optional.empty();
			String equation = "";
			if (evaluationNewModel != null) {
				configOpt = climateConfigurationRepository.findByModelId(evaluationNewModel.getModel().getId());
			}
			if (configOpt.isPresent() && isEnps) {
				equation = getEquationTypeByConfiguration(configOpt.get());
			}

			List<Long> divisionIds = new ArrayList<>();
			JobRole jobRole = jobRoleRepository.getJobroleByEmployeeId(employeeId);
			if (jobRole != null) {
				divisionIds.add(jobRole.getDivision().getId());

				if (divisionIds.size() > 0) {
					resultsByDimension = climateRedshiftDao.getDivisionsWithDimensionOrFactorAvg(companyId,
							evaluationId, null, "dimension_id", "dimension_name", isEnps, divisionIds, equation);

					resultsByFactor = climateRedshiftDao.getDivisionsWithDimensionOrFactorAvg(companyId, evaluationId,
							null, "factor_id", "factor_name", isEnps, divisionIds, equation);
				}

				questionResults = climateRedshiftDao.getQuestionsDetailExcel(companyId, evaluationId,
						jobRole.getDivision().getId(), null, isEnps, equation);				
			}
		}
		return excelHandler.getClimateExcelReport(null, resultsByDimension, resultsByFactor, questionResults, null,
				null, companyId);
	}

}
