# local-flink-with-docker
Uses Apache Flink docker image with Kinesalite (and maybe Kafka)

1. [Install IntelliJ](https://www.jetbrains.com/help/idea/installation-guide.html)
2. [Install Docker](https://docs.docker.com/engine/install/)
3. Run included Docker Image
4. Set up local kinesis stream
5. Clone code samples
6. Modify Code Samples
7. Run Code Samples
8. Enjoy



# Install IntelliJ
This is where you will run the code in this repo--you can choose to use any IDE you'd like, but IntelliJ is uniquely suited for running Flink workloads in that it will spin up a mini Apache Flink cluster on your behalf.

[Install IntelliJ](https://www.jetbrains.com/help/idea/installation-guide.html)

# Install Docker 
Docker will allow you to run a Kinesalite image locally on your machine!
 
[Install Docker](https://docs.docker.com/engine/install/)

# Download the 
```bash
aws kinesis create-stream --endpoint-url https://localhost:4567 --stream-name my-local-stream --shard-count 6 --no-verify-ssl
```

```bash
aws kinesis put-record --endpoint-url https://localhost:4567 --stream-name my-local-stream --data stephanie --partition-key 123 --no-verify-ssl
```

