package com.capgemini.payloadmanagement.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capgemini.payloadmanagement.dao.PayloadRepository;
import com.capgemini.payloadmanagement.exceptions.BadRequestException;
import com.capgemini.payloadmanagement.exceptions.SchemaNotFountException;
import com.capgemini.payloadmanagement.model.PayloadEntity;
import com.capgemini.payloadmanagement.service.PayloadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rohithkumar Senthilkumar
 */

@RestController
@CrossOrigin
public class PayloadManagementController {

	PayloadService payloadService;

	@Autowired
	public PayloadManagementController(PayloadService payloadService) {
		this.payloadService = payloadService;
	}

	@PostMapping(path = "/schema/register")
	public Map<String, Object> registerPayload(@RequestParam("schemaId") String schemaId,
			@RequestParam(required = false) String description, @RequestBody HashMap<String, Object> request) {
		return payloadService.registerPayload(schemaId, description, request);
	}

	@PutMapping(path = "/schema/update")
	public Map<String, Object> updatePayload(@RequestParam("schemaId") String schemaId,
			@RequestParam(required = false) String description, @RequestBody HashMap<String, Object> request) {
		return payloadService.updatePayloadSchema(schemaId, description, request);
	}

	@GetMapping(path = "/schema/get")
	public ResponseEntity<JsonNode> getPayload(@RequestParam("schemaId") String schemaId) {
		return new ResponseEntity<>(payloadService.getPayloadSchema(schemaId), HttpStatus.OK);
	}

	@PostMapping(path = "/schema/map")
	public ResponseEntity<JsonNode> validatePayload(@RequestBody HashMap<String, Object> request,
			@RequestParam("schemaId") String schemaId) {
		return new ResponseEntity<>(payloadService.validatePayload(request, schemaId), HttpStatus.OK);
	}

}
