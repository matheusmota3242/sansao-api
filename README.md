## Setting Mongo database
### Pull Docker Mongo image
```
docker pull mongo:8.0
```

### Docker Compose
Create Docker Compose .yml file
```
version: '3.8'  
 
services:
  mongodb:
    image: mongo:8.0
    container_name: sansaodb
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: sansaoadmin
    volumes:
      - mongo-data:/data/db
 
volumes:
  mongo-data:

```
Go to the folder where the docker-compose.yml file is stored
```
docker compose up -d
```

### Execute Mongo shell
```
docker exec -it sansaodb mongosh -u sansao -p sansaoadmin
```

### Create database user

```
use sansaodb;
```
```
db.createUser({
  user: 'admin',
  pwd: 'sansaoadmin',
  roles: [{ role: 'readWrite', db: 'sansaodb' }]
});
```