# Range-Partitioning Sort

A exercises to implement range partitioning sort.

Reference:
22.2.1 Range-Partitiong Sort
Database System Concepts (7ed) by Silberschatz etc.


## Flow:

1. Start collection service


2. Start partitioning


3. End collection service (signal ending)
- How exactly can we do this?



4. Sort each partition

## Solutions

* Mount local directory into pod in minicube:

https://stackoverflow.com/questions/48534980/mount-local-directory-into-pod-in-minikube

Command:

`minikube mount ${HOME}/workspace/range-partitioning-sort/test:/host`

This command will expose the host directory under `/host` in minicube.

Then add `volumeMounts` and `volumes` configurations in k8s configuration file.

