package com.acsendo.api.customreports.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acsendo.api.customreports.dto.FiltersResultsDTO;
import com.acsendo.api.customreports.dto.LabelBoxDTO;
import com.acsendo.api.customreports.dto.NineBoxResultDTO;
import com.acsendo.api.customreports.dto.PerformanceDTO;
import com.acsendo.api.customreports.dto.PerformanceDetailEmployeeDTO;
import com.acsendo.api.customreports.dto.PerformanceFiltersDTO;
import com.acsendo.api.customreports.dto.PeriodDTO;
import com.acsendo.api.customreports.dto.PeriodDetailDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.service.PerformanceReportsService;
import com.acsendo.api.hcm.dto.PageableResponse;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * Contiene todos los servicios de desempeño usados en el reporteador
 *
 */
@RestController
@RequestMapping(PerformanceReportsController.MAIN_PATH)
public class PerformanceReportsController {

	public static final String MAIN_PATH = "customreports/{companyId}/performance";

	public static final String SEMAPHORE = "/semaphore";

	public static final String NINEBOX = "/ninebox";

	public static final String CONFIGURATION = NINEBOX + "/configuration";

	public static final String PERIODS = "/periods";

	public static final String PERIODS_ID = PERIODS + "/{periodId}";

	public static final String NINEBOX_EMPLOYEE = NINEBOX + "/employee";

	public static final String EMPLOYEE = "/employees/{employeeId}";

	public static final String RESULTS = "/results";

	public static final String AVG = "/avg";

	public static final String TEMPLATES = "/templates";

	public static final String DOWNLOAD = "/download";

	public static final String EXCEL_REPORT = "/report";

	@Autowired
	private PerformanceReportsService performanceReportsService;

	@ApiOperation(value = "Obtiene información de configuración de resultados de desempeño, pesos y semáforo")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(SEMAPHORE)
	public PerformanceDTO getPerformanceConfiguration(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId) {
		try {
			return performanceReportsService.getPerformanceConfiguration(companyId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Obtiene la configuración del ninebox, labels, cuadrantes etc..")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(CONFIGURATION)
	public LabelBoxDTO getConfigurationsNinebox(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "isOkrs", value = "Booleano para saber si la información a devolver es de metas o de Okrs", required = false) @RequestParam(required = false) Boolean isOkrs) {
		try {
			return performanceReportsService.getLabelsAndConfigurationsBoxNinebox(companyId, isOkrs);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Obtiene la información de metas y competencias para ninebox.")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(NINEBOX)
	public List<NineBoxResultDTO> getNineboxResultsGroupByDivision(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@RequestBody FiltersResultsDTO filters) {
		try {
			return performanceReportsService.getNineBoxResultGroupByDivision(companyId, filters);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Método que obtiene los periodos dinámicos")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(PERIODS)
	public List<PeriodDTO> getPerformancePeriodsByCompany(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = false) @RequestParam(required = false) Long employeeId) {
		try {
			return performanceReportsService.getPerformancePeriodsByCompany(companyId, employeeId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Elimina un periodo dinámico")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@DeleteMapping(PERIODS_ID)
	public void deletePerformancePeriodById(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del periodo dinámico", required = true) @PathVariable Long periodId) {
		try {
			performanceReportsService.deletePerformancePeriod(periodId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Obtiene la información de metas y competencias para ninebox por empleado.")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(NINEBOX_EMPLOYEE)
	public List<NineBoxResultDTO> getNineboxResultsGroupByEmployee(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@RequestBody FiltersResultsDTO filters) {
		try {
			if (filters.getIsLeader() == null) {
				filters.setIsLeader(false);
			}
			return performanceReportsService.getNineBoxResultGroupByEmployee(companyId, filters);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Método que guarda un período de desempeño dinámico, con sus respectivos procesos")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(PERIODS)
	public boolean savePerformancePeriodsByCompany(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@RequestBody PeriodDetailDTO dtoPeriod) {
		try {
			return performanceReportsService.savePerformancePeriod(companyId, dtoPeriod);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	@ApiOperation(value = "Obtiene el detalle de un periodo dinámico")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(PERIODS_ID)
	public PeriodDetailDTO getPerformancePeriodById(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del periodo dinámico", required = true) @PathVariable Long periodId) {
		try {
			return performanceReportsService.getPeriodDetailById(periodId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Obtiene el performance de un empleado dependiendo de la configuración")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(EMPLOYEE)
	public PerformanceDetailEmployeeDTO getPerformanceByEmployee(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado", required = true) @PathVariable Long employeeId,
			@ApiParam(name = "periodId", value = "Identificador del periodo dinamico", required = false) @RequestParam(required = false) Long periodId,
			@ApiParam(name = "isLeader", value = "Identificador de si es líder o no", required = false) @RequestParam(required = false) Boolean isLeader,
			@ApiParam(name = "goalPeriodId", value = "Identificador de un período de metas", required = false) @RequestParam(required = false) Long goalPeriodId,
			@ApiParam(name = "evaluationId", value = "Identificador de una evaluación de competencias", required = false) @RequestParam(required = false) Long evaluationId) {
		try {
			return performanceReportsService.getAverageAndDetailPerformanceEmployee(companyId, employeeId, periodId,
					isLeader != null ? isLeader : false, goalPeriodId, evaluationId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Obtiene el listado de resultados de performance para los empleados de una compañía dependiendo de su configuración")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(RESULTS)
	public <T> PageableResponse<T> getEmployeeResultsPerformance(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del periodo dinamico", required = false) @RequestParam(required = false) Long periodId,
			@ApiParam(name = "pageable", value = "Paginación", required = true) Pageable pageable,
			@ApiParam(name = "employeeId", value = "Identificador de empleado (líder)", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "goalPeriodId", value = "Identificador de un período de metas", required = false) @RequestParam(required = false) Long goalPeriodId,
			@ApiParam(name = "evaluationId", value = "Identificador de una evaluación de competencias", required = false) @RequestParam(required = false) Long evaluationId,
			@RequestBody PerformanceFiltersDTO filters) {
		try {

			return performanceReportsService.getEmployeeResultsPerformance(companyId, periodId, pageable, filters,
					employeeId, goalPeriodId, evaluationId);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Obtiene el promedio general de desempeño de la empresa, o el resultado para un líder", response = ResultDTO.class)
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(AVG)
	public Map<String, String> getGeneralAvgPerformance(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "periodId", value = "Identificador del periodo dinamico", required = false) @RequestParam(required = false) Long periodId,
			@ApiParam(name = "employeeId", value = "Identificador de empleado (líder)", required = false) @RequestParam(required = false) Long employeeId,
			@ApiParam(name = "goalPeriodId", value = "Identificador de un período de metas", required = false) @RequestParam(required = false) Long goalPeriodId,
			@ApiParam(name = "evaluationId", value = "Identificador de una evaluación de competencias", required = false) @RequestParam(required = false) Long evaluationId) {
		try {
			return performanceReportsService.getGeneralAvgPerformance(companyId, periodId, employeeId, goalPeriodId,
					evaluationId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Obtiene id de plantilla de reporte de desempeño")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(TEMPLATES)
	public Long getPdfReportTemplate(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación de competencias", required = false) @RequestParam(required = false) Long evaluationId,
			@ApiParam(name = "periodId", value = "Identificador del período de metas", required = false) @RequestParam(required = false) Long periodId) {
		try {
			return performanceReportsService.getPdfReportTemplate(companyId, evaluationId, periodId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ApiOperation(value = "Método que verifica si existe un reporte de desempeño configurado para mostrar botón de descarga")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(DOWNLOAD)
	public boolean showDownloadButton(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluación de competencias", required = false) @RequestParam(required = false) Long evaluationId) {
		try {
			return performanceReportsService.showDownloadButton(companyId, evaluationId);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	@ApiOperation(value = "Obtiene reporte de excel de Desempeño antiguo")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(EXCEL_REPORT)
	public ResponseEntity<byte[]> getPerformanceExcelReport(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = false) @PathVariable Long companyId,
			@RequestBody FiltersResultsDTO filters) {
		try {
			byte[] excelReport = performanceReportsService.getPerformanceExcelReport(companyId,
					filters.getEvaluationId(), filters.getPeriodId(), filters.getIsOkrs(), filters.getCalibrated());
			return new ResponseEntity<byte[]>(excelReport, HttpStatus.OK);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
