# Simão

## Database

Pull Docker image

````
docker pull postgres:alpine
````

Create and run a new Docker container
````
docker run --name <container_name> -e POSTGRES_PASSWORD=<password> -p 5432:5432 -d postgres:alpine
````

Execute psql inside container
````
docker exec -it <container_name> psql -U postgres
````

Create database
````
CREATE DATABASE simaodb;
````

Crate user
````
CREATE USER <user> WITH PASSWORD '<password>';
````

Grant privileges to user
````
GRANT ALL PRIVILEGES ON DATABASE simaodb TO <user>;
GRANT SELECT ON TABLE public.chat_record TO <user>;
````

## Features

- Personal goals
- TODO list
- Water controller

## Waha
````
docker run -it --env-file "$(pwd)/.env" -v "$(pwd)/sessions:/app/.sessions" --rm -p 3000:3000 --name waha devlikeapro/waha
````

## Gemini CLI

### Console

Building gemini-cli image from root folder (where Dockerfile is located)
````
docker build -t gemini-image .
````

Running gemini-cli image

### PowerShell
````
docker run -it --rm --name gemini -v "${PWD}:/workspace" -e "GEMINI_API_KEY=SEU_API_KEY_AQUI" gemini-image
````

### Ubuntu