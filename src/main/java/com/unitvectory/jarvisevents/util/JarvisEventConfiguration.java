package com.unitvectory.jarvisevents.util;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class JarvisEventConfiguration {

	private static final String jarvisEventsTableName = "jarvisevents";

	private static JarvisEventConfiguration config;

	private final Table jarvisEventsTable;

	public JarvisEventConfiguration() {
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		DynamoDB dynamodb = new DynamoDB(client);
		this.jarvisEventsTable = dynamodb.getTable(jarvisEventsTableName);
	}

	public synchronized static JarvisEventConfiguration getInstance() {
		if (config == null) {
			config = new JarvisEventConfiguration();
		}

		return config;
	}

	/**
	 * @return the jarvisEventsTable
	 */
	public Table getJarvisEventsTable() {
		return jarvisEventsTable;
	}
}
