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
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.aggregation.Aggregations;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.kinesis.shaded.com.amazonaws.SDKGlobalConfiguration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;
import org.apache.flink.util.Collector;

import java.util.Properties;


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
public class StreamingJobWithKinesis {

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


		DataStreamSource<WorkProfile> workProfileDataStreamSource = env.addSource(new FlinkKinesisConsumer("my-local-stream", new WorkProfileDeserialization(), kinesisProps));

		SingleOutputStreamOperator<WorkProfile> seniorAnimals = workProfileDataStreamSource.filter(x -> x.getYearsWorked() > 10).map(x ->
				new WorkProfile(x.getColor(), x.getAnimal(), "Senior " + x.getPosition(), x.getYearsWorked())

		);

		SingleOutputStreamOperator averageYearsByAnimal = workProfileDataStreamSource.keyBy(x -> x.getAnimal())
				.window(TumblingProcessingTimeWindows.of(Time.seconds(10))).apply(new YearsWorkedAverager());

		averageYearsByAnimal.print();
		// execute program
		env.execute("My Flink Application");
	}


	public static class YearsWorkedAverager implements WindowFunction<WorkProfile, Tuple2<String, Double>, String, TimeWindow>
	{

		@Override
		public void apply(String animalType, TimeWindow timeWindow,
						  Iterable<WorkProfile> workProfiles,
						  Collector<Tuple2<String, Double>> collector) throws Exception {
			int count = 0;
			double sum = 0.0;
			for(WorkProfile wp : workProfiles)
			{
				count++;
				sum += wp.getYearsWorked();
			}

			double averageYearsWorked = sum / count;

			collector.collect(new Tuple2<>(animalType, averageYearsWorked));
		}
	}

}
