package com.capgemini.payloadmanagement.service;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Rohithkumar Senthilkumar
 */
public interface PayloadService {

	JsonObject mapPayload(JsonObject json, Map<String, Object> request, Integer arrayIteration);

	Object getValue(String dataType, JsonElement path, Map<String, Object> request, String key, Integer arrayIteration);

	JsonArray getJsonArrayFromRequest(String path, Map<String, Object> request);

	Boolean validateRequest(JsonObject json, Integer arrayIteration, String arrayKey);

	Map<String, Object> registerPayload(String schemaId, String description, HashMap<String, Object> request);
	
	Map<String, Object> updatePayloadSchema(String schemaId, String description, HashMap<String, Object> request);
	
	JsonNode getPayloadSchema(String schemaId);
	
	JsonNode validatePayload(HashMap<String, Object> request,
			String schemaId);
}
