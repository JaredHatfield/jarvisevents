package com.unitvectory.jarvisevents;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.json.JSONObject;
import com.unitvectory.jarvisevents.util.JarvisEventConfiguration;

public class JarvisEventHandler implements RequestStreamHandler {

	private Map<String, String> authMap = new HashMap<String, String>();

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		try {
			// Get the configuration
			Table table = JarvisEventConfiguration.getInstance().getJarvisEventsTable();

			// Parse the input into a JSON object
			String jsonString = IOUtils.toString(inputStream);
			JSONObject json = new JSONObject(jsonString);

			String deviceId = json.getString("deviceId");
			long epochMilliseconds = System.currentTimeMillis();
			json.put("epochMilliseconds", epochMilliseconds);

			// Verify the request is authorized
			String auth = json.getString("auth");
			String authActual = authMap.get(deviceId);
			if (authActual == null) {
				Item authItem = table.getItem(new GetItemSpec()
						.withPrimaryKey("deviceId", deviceId, "epochMilliseconds", 0).withAttributesToGet("auth"));
				if (authItem != null) {
					authActual = authItem.getString("auth");
					if (authActual != null) {
						authMap.put(deviceId, authActual);
					}
				}
			}

			if (authActual == null) {
				throw new RuntimeException("Unauthorized");
			}

			if (!authActual.equals(auth)) {
				throw new RuntimeException("Unauthorized");
			}

			json.remove("auth");

			// If this has a sensorId restructure the hash key to break up
			// sensors
			if (json.has("sensorId")) {
				String sensorId = json.getString("sensorId");
				if (deviceId.contains("|") || sensorId.contains("|")) {
					throw new RuntimeException("| not allowed");
				}

				json.put("deviceId_actual", deviceId);
				json.put("deviceId", deviceId + "|" + sensorId);
			}

			// Remove all of the empty keys
			List<String> emptyKeys = new ArrayList<String>();

			@SuppressWarnings("unchecked")
			Iterator<String> i = (Iterator<String>) json.keys();
			while (i.hasNext()) {
				String key = i.next();
				String value = json.getString(key);
				if (value.trim().length() == 0) {
					emptyKeys.add(key);
				}
			}

			for (String key : emptyKeys) {
				json.remove(key);
			}

			Item item = Item.fromJSON(json.toString());

			// Write to DynamoDB
			table.putItem(item);

			// Write the output
			JSONObject out = new JSONObject();
			out.put("result", "success");
			IOUtils.write(out.toString(), outputStream);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Internal Error", e);
		}
	}

}
