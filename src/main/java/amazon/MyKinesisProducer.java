package amazon;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import myflinkapp.pojo.WorkProfile;
import org.apache.flink.kinesis.shaded.com.amazonaws.SDKGlobalConfiguration;
import org.apache.flink.kinesis.shaded.com.amazonaws.services.kinesis.producer.Attempt;
import org.apache.flink.kinesis.shaded.com.amazonaws.services.kinesis.producer.KinesisProducer;
import org.apache.flink.kinesis.shaded.com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import org.apache.flink.kinesis.shaded.com.amazonaws.services.kinesis.producer.UserRecordResult;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MyKinesisProducer {

    public static Faker FAKER = new Faker();

    public static void main(String[] args) throws UnsupportedEncodingException, ExecutionException, InterruptedException {


        System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
        System.setProperty("org.apache.flink.kinesis.shaded.com.amazonaws.sdk.disableCertChecking", "true");

        KinesisProducerConfiguration config = new KinesisProducerConfiguration()
                .setKinesisEndpoint("localhost")
                .setKinesisPort(4567)
                .setVerifyCertificate(false)
                .setRegion("us-east-1");

        KinesisProducer kinesis = new KinesisProducer(config);

        // Put some records and save the Futures
        List<Future<UserRecordResult>> putFutures = new LinkedList<Future<UserRecordResult>>();



        Gson gson = new GsonBuilder().create();

        while(true)
        {
            sendAnimalDataToKinesis(kinesis, putFutures, gson);
            Thread.sleep(1000);
        }

    }

    private static void sendAnimalDataToKinesis(KinesisProducer kinesis, List<Future<UserRecordResult>> putFutures, Gson gson) throws UnsupportedEncodingException, InterruptedException, ExecutionException {
        for(int i = 0; i < 1000; i++)
        {
            //color, animal, job position, years worked
            WorkProfile workingAnimal = new WorkProfile(FAKER.color().name(),
                    FAKER.animal().name(),
                    FAKER.job().position(),
                    FAKER.number().numberBetween(1, 100));
            System.out.println(gson.toJson(workingAnimal));
            ByteBuffer data = ByteBuffer.wrap(gson.toJson(workingAnimal).getBytes("UTF-8"));
            // doesn't block
            putFutures.add(
                    kinesis.addUserRecord("my-local-stream", "myPartitionKey", data));

        }

        // Wait for puts to finish and check the results
        for (Future<UserRecordResult> f : putFutures) {
            UserRecordResult result = f.get(); // this does block
            if (result.isSuccessful()) {
                System.out.println("Put record into shard " +
                        result.getShardId());
            } else {
                for (Attempt attempt : result.getAttempts()) {
                    // Analyze and respond to the failure
                    System.out.println(attempt.getErrorMessage());
                }
            }
        }
    }
}
