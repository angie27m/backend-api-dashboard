package com.acsendo.api.customreports.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.service.ExtraGraphicsService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * Contiene todos los servicios de las graficas extra usados en el reporteador 
 *
 */
@RestController
@RequestMapping(ExtraGraphicsController.MAIN_PATH)
public class ExtraGraphicsController {

	public static final String MAIN_PATH = "customreports/{companyId}/graphics";
	
	@Autowired
	private ExtraGraphicsService extraGraphicsService;
	
	@ApiOperation(value = "Obtiene las graficas extra de reporteador seleccionadas por el usuario ")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping()
	public List<ResultDTO> getGraphicsConfiguration(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "userId", value = "Identificador de usuario creador de la vacante", required = true) @RequestParam Long userId,
			@ApiParam(name = "moduleName", value = "Nombre del modulo al cual pertenece los permisos", required = true) @RequestParam String moduleName) {
		try {
			 return extraGraphicsService.getGraphicsConfiguration(companyId, userId, moduleName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ApiOperation(value = "guarda las graficas extras de reporteador que desea ver el usuario ")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping()
	public boolean saveGraphicsConfiguration(@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "userId", value = "Identificador de usuario creador de la vacante", required = true) @RequestParam Long userId,
			@ApiParam(name = "moduleName", value = "Nombre del modulo al cual pertenece los permisos", required = true) @RequestParam String moduleName,
			@RequestBody List<String> graphicConfigutarion) {
		try {
			 return extraGraphicsService.saveGraphicsConfiguration(graphicConfigutarion,companyId, userId, moduleName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
