# CS6650-DSBS-Repo
- Designed RESTful web application that allow supermarket to process purchase requests and keep track of purchase order data
- System structure: multi-thread client and server that utilized Kafka and Redis cache, deployed on EC2 and adopt AWS application layer load balancer with distributed RDS
- Passed stress test on successfully processing total 1.4 million requests with achieved around 8700 (request/sec) throughput, by
comparing to an initial design of 2100 (request/sec) on total 700K requests.
