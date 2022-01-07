/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package myflinkapp;

import myflinkapp.pojo.WorkProfile;
import myflinkapp.pojo.WorkProfileDeserialization;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.kinesis.shaded.com.amazonaws.SDKGlobalConfiguration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;

import java.util.Properties;
import java.util.Random;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class Kinesis_StreamingJob {

	public static void main(String[] args) throws Exception {
		// REQUIRED SETTINGS FOR LOCAL KINESIS
		System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
		System.setProperty("org.apache.flink.kinesis.shaded.com.amazonaws.sdk.disableCertChecking", "true");


		// set up the streaming execution environment
		Configuration flinkConfig = new Configuration();
		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(flinkConfig);
		env.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE );
		env.setParallelism(6);

		// set up kinesis consumer pointing to local kinesis instance
		Properties kinesisProps = new Properties();
		kinesisProps.setProperty(ConsumerConfigConstants.AWS_REGION, "us-east-1");
		kinesisProps.put(ConsumerConfigConstants.AWS_ACCESS_KEY_ID, "fake_access_key");
		kinesisProps.put(ConsumerConfigConstants.AWS_SECRET_ACCESS_KEY, "fake_secret_access_key");
		kinesisProps.setProperty(ConsumerConfigConstants.AWS_ENDPOINT, "https://localhost:4567");
		kinesisProps.setProperty(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "TRIM_HORIZON");


		DataStreamSource<String> stringSource = env.addSource(new FlinkKinesisConsumer("my-local-stream", new SimpleStringSchema(), kinesisProps));


		stringSource
				.map(x -> x.length())
				.print();

		// execute program
		env.execute("My Flink Application");
	}
}
