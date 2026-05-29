
docker build -t eureka-server:1.0 ./eureka-server
docker build -t gym-crm:1.0 ./gym-crm-service
docker build -t trainer-workload:1.0 ./trainer-workload-service
# Eureka
docker run -d --name eureka-app -p 8761:8761 eureka-server:1.0

# Main project
docker run -d --name gym-crm-app -p 8080:8080 gym-crm:1.0

# Workload
docker run -d --name workload-app -p 8081:8081 trainer-workload:1.0