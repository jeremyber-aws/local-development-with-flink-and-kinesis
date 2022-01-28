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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

public class ProductionReadyStreamingJob {

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		int MAX_PARALLELISM = env.getParallelism();


		SingleOutputStreamOperator<String> stringSource = env.addSource(new SourceFunction<String>() {
														 @Override
														 public void run(SourceContext<String> sourceContext) throws Exception {
															 while (true) {
																 Random rand = new Random();
																 sourceContext.collect(RandomStringUtils.randomAlphabetic(rand.nextInt(100)));
																 Thread.sleep(1000);
															 }
														 }

														@Override
														public void cancel() {

														}
		}).uid("my-custom-source").setParallelism(1);


		stringSource
				.map(x -> x.length())
					.uid("my-map-operator")
					.setParallelism(Math.round(MAX_PARALLELISM/2))
				.print()
					.uid("my-print-statement")
					.setParallelism(Math.round(MAX_PARALLELISM));

		// execute program
		env.execute("My Flink Application");
	}
}
