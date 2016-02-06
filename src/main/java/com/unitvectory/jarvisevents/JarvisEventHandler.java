package com.unitvectory.jarvisevents;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class JarvisEventHandler implements RequestHandler<String, String> {

	public String handleRequest(String request, Context context) {
		return request;
	}

}
