package com.unitvectory.jarvisevents;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class JarvisEventHandler implements RequestStreamHandler {

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		String jsonString = IOUtils.toString(inputStream);
		System.out.println(jsonString);
		IOUtils.write(jsonString, outputStream);
	}

}
