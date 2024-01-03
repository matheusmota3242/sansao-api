package com.m2g2.controller;

import com.m2g2.constants.EndpointConstant;
import com.m2g2.dto.request.TrainingRequest;
import com.m2g2.dto.response.TrainingResponse;
import com.m2g2.model.error.ErrorResponse;
import com.m2g2.service.TrainingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/training")
public class TrainingController {

	private final Logger logger = LoggerFactory.getLogger(TrainingController.class);
	private final TrainingService service;

	public TrainingController(TrainingService service) {
		this.service = service;
	}
	
	@PostMapping
	public void save(@RequestBody @Valid TrainingRequest request) {
		logger.info(EndpointConstant.ENTRADA_COM_ARGUMENTO, "save", request);
		service.save(request);
		logger.info(EndpointConstant.SAIDA_COM_ARGUMENTO, "save", request);
	}
	
	@GetMapping
	public List<TrainingResponse> getAll() {
		logger.info(EndpointConstant.ENTRADA_SEM_ARGUMENTO, "getAll");
		List<TrainingResponse> trainingResponses = service.getAll();
		logger.info(EndpointConstant.SAIDA_COM_ARGUMENTO, "getAll", trainingResponses);
		return trainingResponses;
	}

	@ExceptionHandler({MethodArgumentNotValidException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		String message = "Erro de validação indefinido para os campos de entrada.";
		if (e.getAllErrors().stream().noneMatch(err -> err == null || err.getDefaultMessage() == null)) {
			List<String> messages = e.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
			message = String.format("Erro de validação para o(s) campo(s) de entrada. Detalhe: %s.", String.join(". ",
					messages));
		}
		logger.error(message);
		return new ErrorResponse(message);
	}

}