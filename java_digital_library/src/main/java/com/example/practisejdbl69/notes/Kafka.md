## Working with KAFKA

- Go to bin path of kafka libraries
  1. Run Zookeeper: Starts on 2181 port
  ```
    ./zookeeper-server-start <path>/config/zookeeper.properties
  ```
  MAC:
  ```
    sathwick@c889f3ed24c1 ~ % cd /opt/homebrew/opt/kafka/bin/
    sathwick@c889f3ed24c1 bin % zookeeper-server-start /opt/homebrew/etc/kafka/zookeeper.properties 
  ```
  2. Start kafka
  ```
    ./kafka-server-start  <path>/config/kafka.properties
  ```
  MAC:
  ```
    /opt/homebrew/opt/kafka/bin/kafka-server-start /opt/homebrew/etc/kafka/server.properties
  ```
  3. Create a topic then only subscriber can subscribe and poll the content. 
  ```
    ./kafka-topics --create --topic jbdl69 --bootstrap-server localhost:9092
  ```
  MAC:
  ```
    sathwick@c889f3ed24c1 ~ % cd /opt/homebrew/opt/kafka/bin/
    sathwick@c889f3ed24c1 bin % kafka-topics --bootstrap-server=localhost:9092 --topic USER_CREATED --create
  ```
  ```
    ./kafka-topics --list --bootstrap-server localhost:9092
  ```
  - to alter the topic we can use below command, say to configure the no. of paritions
  ```
    ./kafka-topics --alter --topic jbdl69 --partitions 2 --bootstrap-server localhost:9092
  ```

  3. Create as many consumers as possible as per requirement and subscribe to topic, and it will be in waiting state
  ```
    ./kafka-console-consumer --topic jbdl69 --bootstrap-server localhost:9092
  ```
  - if any consumer want's to read all the messages from beginning even though it subscribed very later in the stage.
  ```
    ./kafka-console-consumer --topic jbdl69 --bootstrap-server localhost:9092 --from-beginning 
  ```
  - To start a consumer under a group we can use --group flag
  ```
    ./kafka-console-consumer --topic jbdl69 --bootstrap-server localhost:9092 --group <group-name>
  ```
  **NOTE: We have a rule that: One partition can be read by only one consumer in consumer groups**
  - Number of consumers in a consumer group <= No. of partitions in KAFKA
  
  4. Create a producer and publish to topic
     ```
       ./kafka-console-producer --topic jbdl69 --bootstrap-server localhost:9092
        >      hi # consumer console show's this message once this message is published
        >      hello 
     ```
  
