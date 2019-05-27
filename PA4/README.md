#Simple Amazon Dynamo

This project deals with the implementation of simplified Dynamo with features such as partitioning, replication and failure handling.
The goal is to ensure availability and linearizability. The implementation performs concurrent read and writes to the system even under failures.At the same time, reads return the most recent writes.