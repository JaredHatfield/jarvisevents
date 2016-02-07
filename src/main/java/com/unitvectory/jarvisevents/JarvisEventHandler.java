package com.unitvectory.jarvisevents;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.json.JSONObject;
import com.unitvectory.jarvisevents.util.JarvisEventConfiguration;

public class JarvisEventHandler implements RequestStreamHandler {

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		try {
			// Get the configuration
			Table table = JarvisEventConfiguration.getInstance().getJarvisEventsTable();

			// Parse the input into a JSON object
			String jsonString = IOUtils.toString(inputStream);
			JSONObject json = new JSONObject(jsonString);
			System.out.println(jsonString);

			String deviceId = json.getString("deviceId");
			long epochMilliseconds = System.currentTimeMillis();
			json.put("epochMilliseconds", epochMilliseconds);

			table.putItem(new PutItemSpec().withItem(Item.fromJSON(json.toString())));

			// Write the output
			JSONObject out = new JSONObject();
			out.put("result", "success");
			IOUtils.write(out.toString(), outputStream);
		} catch (Exception e) {
			throw new RuntimeException("Internal Error", e);
		}
	}

}
