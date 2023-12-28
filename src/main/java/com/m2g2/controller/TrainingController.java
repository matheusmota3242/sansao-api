package com.m2g2.controller;

import com.m2g2.dto.request.TrainingRequest;
import com.m2g2.model.Training;
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
import java.util.stream.Stream;

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
		logger.info("[ENTRADA] Endpoint 'save'. Objeto: {}", request);
		service.save(request);
		logger.info("[SAIDA] Endpoint 'save'. Objeto: {}", request);
	}
	
	@GetMapping
	public List<Training> getAll() {
		return service.getAll();
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