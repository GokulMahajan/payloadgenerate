package com.capgemini.payloadmanagement.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.capgemini.payloadmanagement.dao.PayloadRepository;
import com.capgemini.payloadmanagement.exceptions.BadRequestException;
import com.capgemini.payloadmanagement.exceptions.SchemaNotFountException;
import com.capgemini.payloadmanagement.model.PayloadEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Rohithkumar Senthilkumar
 */
@Service
@Slf4j
public class PayloadServiceImpl implements PayloadService {

	PayloadRepository payloadRepository;

	@Autowired
	public PayloadServiceImpl(PayloadRepository payloadRepository) {
		super();
		this.payloadRepository = payloadRepository;
	}

	@Override
	public JsonObject mapPayload(JsonObject json, Map<String, Object> request, Integer arrayIteration) {
		int[] counter = new int[1];
		try (Stream<Entry<String, JsonElement>> stream = json.entrySet().parallelStream()) {
			stream.forEach(entry -> {

				JsonElement value = entry.getValue();

				if (value.isJsonObject() && value.getAsJsonObject().get("type").getAsString() != null) {

					switch (value.getAsJsonObject().get("type").getAsString()) {
					case "String": {
						json.addProperty(entry.getKey(),
								(String) getValue(value.getAsJsonObject().get("type").getAsString(),
										value.getAsJsonObject().get("path"), request, entry.getKey().toString(),
										arrayIteration));
						mapPayload(value.getAsJsonObject(), request, arrayIteration);
					}
						break;
					case "Integer": {
						json.addProperty(entry.getKey(),
								(Integer) getValue(value.getAsJsonObject().get("type").getAsString(),
										value.getAsJsonObject().get("path"), request, entry.getKey().toString(),
										arrayIteration));
						mapPayload(value.getAsJsonObject(), request, arrayIteration);
					}
						break;
					case "Double": {
						json.addProperty(entry.getKey(),
								(Double) getValue(value.getAsJsonObject().get("type").getAsString(),
										value.getAsJsonObject().get("path"), request, entry.getKey().toString(),
										arrayIteration));
						mapPayload(value.getAsJsonObject(), request, arrayIteration);

					}
						break;
					case "Map": {

						value.getAsJsonObject().remove("type");
						value.getAsJsonObject().remove("path");
						mapPayload(value.getAsJsonObject(), request, arrayIteration);
					}
						break;

					default: {

						json.addProperty(entry.getKey(), "gg");
					}
					}
				} else if (value.isJsonArray()) {

					JsonArray array = new JsonArray();
					switch (value.getAsJsonArray().get(0).getAsJsonObject().get("mappingType").getAsString()) {
					case "handshake": {
						if (value.getAsJsonArray().get(0).getAsJsonObject().get("type").getAsString().equals("Map")) {
							try {
								value.getAsJsonArray().get(0).getAsJsonObject().remove("type");
								value.getAsJsonArray().get(0).getAsJsonObject().remove("mappingType");

								List<JsonObject> list = new Gson().fromJson(
										getJsonArrayFromRequest(value.getAsJsonArray().get(0).getAsJsonObject()
												.get("path").getAsString(), request).toString(),
										new TypeToken<List<JsonObject>>() {
										}.getType());
								value.getAsJsonArray().get(0).getAsJsonObject().remove("path");

								try (Stream<JsonObject> handshakeStream = list.stream()) {
									handshakeStream.forEach(e -> {
										JsonNode node = null;
										try {
											node = new ObjectMapper().readTree(
													new Gson().toJson(value.getAsJsonArray().get(0).getAsJsonObject()));
										} catch (JsonMappingException ex) {
											ex.printStackTrace();
										} catch (JsonProcessingException ex) {
											ex.printStackTrace();
										}
										array.add(mapPayload(JsonParser.parseString(node.toString()).getAsJsonObject(),
												request, counter[0]));
										counter[0]++;
									});
								}
								json.add(entry.getKey(), array);
							} catch (JsonSyntaxException ex) {
								if (ex.getMessage().toString().contains(
										"Expected a com.google.gson.JsonObject but was com.google.gson.JsonPrimitive"))
									throw new BadRequestException(
											ex.getMessage().toString().replace("com.google.gson.", "") + " from "
													+ value.getAsJsonArray().get(0).getAsJsonObject().get("path")
															.getAsString());
								else
									throw new BadRequestException(ex.getMessage().toString());
							} catch (IllegalStateException ex) {
								if (ex.getMessage().toString().contains("Not a JSON Object:"))
									throw new BadRequestException("Invalid data at the path: "
											+ value.getAsJsonArray().get(0).getAsJsonObject().get("path").getAsString()
											+ ", Expected an array");
								else
									throw new BadRequestException(ex.getMessage().toString());
							}
						}
					}
						break;
					case "merge": {
						try {
							json.add(entry.getKey(),
									getJsonArrayFromRequest(
											value.getAsJsonArray().get(0).getAsJsonObject().get("path").getAsString(),
											request));
						} catch (IllegalStateException ex) {
							if (ex.getMessage().toString().contains("Not a JSON Array:"))
								throw new BadRequestException("Invalid data at the path: "
										+ value.getAsJsonArray().get(0).getAsJsonObject().get("path").getAsString()
										+ ", Expected an array");
							else
								throw new BadRequestException(ex.getMessage().toString());
						}

					}
						break;
					case "polling": {

						try (Stream<JsonElement> pollingStream = value.getAsJsonArray().asList().stream()) {
							pollingStream.forEach(element -> {
								try {
									element.getAsJsonObject().remove("type");
									element.getAsJsonObject().remove("mappingType");
									element.getAsJsonObject().remove("path");
									JsonNode node = null;
									try {
										node = new ObjectMapper()
												.readTree(new Gson().toJson(element.getAsJsonObject()));
									} catch (JsonMappingException e) {
										e.printStackTrace();
									} catch (JsonProcessingException e) {
										e.printStackTrace();
									}

									array.add(mapPayload(JsonParser.parseString(node.toString()).getAsJsonObject(),
											request, null));
								} catch (NullPointerException e) {
									throw new BadRequestException("Value for " + entry.getKey() + "[" + arrayIteration
											+ "] was not found in the registered path: "
											+ element.getAsJsonObject().get("path").toString());
								}
							});
						}
						json.add(entry.getKey(), array);

					}
						break;
					}
				}
			});
		}
		return json;

	}

	@Override
	public Object getValue(String dataType, JsonElement path, Map<String, Object> request, String key,
			Integer arrayIteration) {
		log.info("Fetching value for the key: " + key + " from the path: " + path + " with datatype:" + dataType);
		try {
			JsonElement jsonElement = JsonParser.parseString(new Gson().toJson(request));

			String[] pathElements = path.getAsString().split("\\.");
			JsonElement element = jsonElement;
			for (String pathElement : pathElements) {
				if (pathElement.equals("[]"))
					element = element.getAsJsonArray().get(arrayIteration);
				else if (Pattern.compile("^[{][0-9]+[}]$").matcher(pathElement).find())
					element = element.getAsJsonObject()
							.get(new ArrayList<>(element.getAsJsonObject().keySet()).get(Integer.parseInt(pathElement
									.chars()
									.flatMap(c -> Character.isDigit((char) c) ? IntStream.of(c) : IntStream.empty())
									.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
									.toString())));
				else if (Pattern.compile("\\d+").matcher(pathElement).find())
					element = element.getAsJsonArray().get(Integer.parseInt(pathElement));
				else
					element = element.getAsJsonObject().get(pathElement);
			}
			log.info("returning value: " + element);
			switch (dataType) {
			case "Integer":
				return element.getAsInt();
			case "String":

				return element.getAsString();
			case "Double":
				return element.getAsDouble();
			case "Long":
				return element.getAsLong();

			default:
				return element;
			}

		} catch (IllegalStateException e) {
			throw new BadRequestException(
					"Invalid data for the key: " + key + ", consider using a \"" + dataType + "\" data instead.");
		} catch (UnsupportedOperationException e) {
			throw new BadRequestException(
					"Invalid data for the key: " + key + ", consider using a \"" + dataType + "\" data instead.");
		} catch (NumberFormatException e) {
			throw new BadRequestException("Cannot format data " + e.getMessage().toLowerCase() + " under the key: "
					+ key + ", consider using \"" + dataType + "\" data instead.");
		} catch (NullPointerException e) {
			throw new BadRequestException(key + " was not found in the registered path: " + path);
		} catch (Exception e) {
			throw new BadRequestException("Bad request for the key: " + key + " with registered path: " + path
					+ ", error: " + e.getLocalizedMessage());
		}

	}

	@Override
	public JsonArray getJsonArrayFromRequest(String path, Map<String, Object> request) {
		JsonElement jsonElement = JsonParser.parseString(new Gson().toJson(request));
		String[] pathElements = path.split("\\.");
		JsonElement element = jsonElement;
		for (String pathElement : pathElements) {
			if (Pattern.compile("\\d+").matcher(pathElement).find())
				element = element.getAsJsonArray().get(Integer.parseInt(pathElement));
			else
				element = element.getAsJsonObject().get(pathElement);
		}
		return element.getAsJsonArray();
	}

	@Override
	public Boolean validateRequest(JsonObject json, Integer arrayIteration, String arrayKey) {
		String[] allowedTypes = { "Map", "String", "Integer", "Double", "Long" };
		String[] allowedMappingTypes = { "handshake", "polling", "merge" };

		try (Stream<Entry<String, JsonElement>> stream = json.entrySet().parallelStream()) {
			stream.forEach(entry -> {
				JsonElement value = entry.getValue();
				if (value.isJsonObject()) {
					if (value.getAsJsonObject().get("path") == null
							|| value.getAsJsonObject().get("path").getAsString().isEmpty())
						throw new BadRequestException("path is required for the key: " + entry.getKey()
								+ ((arrayIteration != null) ? " in " + arrayKey + "[" + arrayIteration + "]" : ""));
					else {
						if (value.getAsJsonObject().get("type") == null)
							throw new BadRequestException("type is required for the key: " + entry.getKey()
									+ ((arrayIteration != null) ? " in " + arrayKey + "[" + arrayIteration + "]" : ""));
						else if (!Arrays.asList(allowedTypes)
								.contains(value.getAsJsonObject().get("type").getAsString()))
							throw new BadRequestException("Invalid type -> "
									+ value.getAsJsonObject().get("type").getAsString() + " <- provided for the key: "
									+ entry.getKey()
									+ ((arrayIteration != null) ? " in " + arrayKey + "[" + arrayIteration + "]" : ""));
						else {
							validateRequest(value.getAsJsonObject(), arrayIteration, arrayKey);
						}
					}
				}

				else if (value.isJsonArray()) {
					int[] counter = new int[1];
					try (Stream<JsonElement> arrayStream = value.getAsJsonArray().asList().stream()) {
						arrayStream.forEach(e -> {

							if (e.getAsJsonObject().get("path") == null
									|| e.getAsJsonObject().get("path").getAsString().isEmpty())
								throw new BadRequestException(
										"path is required for the key: " + entry.getKey() + "[" + counter[0] + "]");
							else {

								if (e.getAsJsonObject().get("mappingType") == null)
									throw new BadRequestException("mappingType is required for the key: "
											+ entry.getKey() + "[" + counter[0] + "]");
								else if (!Arrays.asList(allowedMappingTypes)
										.contains(e.getAsJsonObject().get("mappingType").getAsString()))
									throw new BadRequestException("Invalid mappingType -> "
											+ e.getAsJsonObject().get("mappingType").getAsString()
											+ " <- provided for the key: " + entry.getKey() + "[" + counter[0] + "]");

								if (e.getAsJsonObject().get("type") == null
										&& !(e.getAsJsonObject().get("mappingType").getAsString().equals("merge")))
									throw new BadRequestException(
											"type is required for the key: " + entry.getKey() + "[" + counter[0] + "]");
								else if (!(e.getAsJsonObject().get("mappingType").getAsString().equals("merge"))
										&& !Arrays.asList(allowedTypes)
												.contains(e.getAsJsonObject().get("type").getAsString()))
									throw new BadRequestException("Invalid type -> "
											+ e.getAsJsonObject().get("type").getAsString() + " <- provided for key: "
											+ entry.getKey() + "[" + counter[0] + "]");
								else if (e.getAsJsonObject().get("mappingType").getAsString().equals("handshake")
										&& !(e.getAsJsonObject().get("type").getAsString().equals("Map")))
									throw new BadRequestException(e.getAsJsonObject().get("type").getAsString()
											+ " type is not allowed for handshake mappingType for the key: "
											+ entry.getKey() + "[" + counter[0] + "]");
								else {
									validateRequest(e.getAsJsonObject(), counter[0], entry.getKey());
								}
							}
							counter[0]++;
						});
					}
				}
			});
		}

		return true;

	}

	@Override
	public Map<String, Object> registerPayload(String schemaId, String description, HashMap<String, Object> request) {

		Map<String, Object> response = new HashMap<>();
		Optional<PayloadEntity> payloadSchema = payloadRepository.findBySchemaId(schemaId);

		if (payloadSchema.isEmpty()) {
			if (JsonParser.parseString(new Gson().toJson(request)).getAsJsonObject().size() < 1)
				throw new BadRequestException("The payload schema cannot be empty");

			else if (validateRequest(JsonParser.parseString(new Gson().toJson(request)).getAsJsonObject(), null,
					null)) {
				PayloadEntity entity = new PayloadEntity();
				entity.setPayloadSchema(new Gson().toJson(request));
				entity.setSchemaId(schemaId);
				entity.setDescription(description != null ? description : "undefined");
				payloadRepository.save(entity);
				response.put("schemaId", schemaId);
				response.put("description", entity.getDescription());
				response.put("status", "REGISTERED SUCCESSFULLY");
			}
			return response;
		} else
			throw new BadRequestException("Schema id already exists");
	}

	@Override
	public Map<String, Object> updatePayloadSchema(String schemaId, String description,
			HashMap<String, Object> request) {
		Map<String, Object> response = new HashMap<>();
		Optional<PayloadEntity> payloadSchema = payloadRepository.findBySchemaId(schemaId);

		if (payloadSchema.isPresent()) {

			if (JsonParser.parseString(new Gson().toJson(request)).getAsJsonObject().size() < 1)
				throw new BadRequestException("The payload schema cannot be empty");

			else if (validateRequest(JsonParser.parseString(new Gson().toJson(request)).getAsJsonObject(), null,
					null)) {

				payloadSchema.get().setPayloadSchema(new Gson().toJson(request));
				payloadSchema.get().setSchemaId(schemaId);
				if (description != null)
					payloadSchema.get().setDescription(description);
				payloadRepository.save(payloadSchema.get());
				response.put("schemaId", payloadSchema.get().getSchemaId());
				response.put("description", payloadSchema.get().getDescription());
				response.put("status", "UPDATED SUCCESSFULLY");
			}
			return response;
		} else
			throw new SchemaNotFountException("Payload schema not found for schemaId: " + schemaId);
	}

	@Override
	public JsonNode getPayloadSchema(String schemaId) {
		Optional<PayloadEntity> payloadSchema = payloadRepository.findBySchemaId(schemaId);
		if (payloadSchema.isPresent()) {
			String payload = payloadSchema.get().getPayloadSchema();
			JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
			JsonNode node = null;
			try {
				node = new ObjectMapper().readTree(new Gson().toJson(json));
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return node;

		} else
			throw new SchemaNotFountException("Payload schema not found for schemaId: " + schemaId);
	}

	@Override
	public JsonNode validatePayload(HashMap<String, Object> request, String schemaId) {
		Optional<PayloadEntity> payloadSchema = payloadRepository.findBySchemaId(schemaId);

		if (payloadSchema.isPresent()) {

			String payload = payloadSchema.get().getPayloadSchema();
			JsonObject json = mapPayload(JsonParser.parseString(payload).getAsJsonObject(), request, null);
			JsonNode node = null;
			try {
				node = new ObjectMapper().readTree(new Gson().toJson(json));
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return node;
		} else
			throw new BadRequestException("Payload Schema does not exist for schemaId: " + schemaId);
	}

}
