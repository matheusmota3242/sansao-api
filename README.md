# Sansao API
A REST API for managing physical activities of users, built with Java 17 and Spring Boot 3.

## Introduction
The Sansao API is designed to help users manage their physical activities. It provides endpoints for user authentication and CRUD operations for workouts. This API is built using Java 17 and Spring Boot 3, and it includes features such as JWT-based authentication, data validation, and MongoDB support.

## Features
- User registration and authentication
- JWT token validation
- CRUD operations for workouts
- Data validation with Jakarta Validation
- Integration with MongoDB for data persistence
- Comprehensive logging
- Requirements
- Java 17
- Maven 3.6+
- MongoDB
- Installation

## Clone the repository:

```
git clone https://github.com/yourusername/sansao-api.git
cd sansao-api
```
## 
Build the project with Maven:

```
mvn clean install
```

Run the application:

```
mvn spring-boot:run
```
## Usage
### Authentication
- **Register**
```
POST /api/v1/auth/register
```
Request Body:
```
{
  "username": "your_username",
  "password": "your_password"
}
```

Response:

```
{
  "token": "jwt_token"
}
```
- **Authenticate**
```
POST /api/v1/auth
```

Request Body:

```
{
  "username": "your_username",
  "password": "your_password"
}
```

Response:

```
{
  "token": "jwt_token"
}
```

- **Validate Token**
```
GET /api/v1/auth/validate
```
## Workouts
### Create Workout
```
POST /api/v1/workouts
```
Request Body:

```
{
    "start": "16-06-2024 00:00",
    "end": "17-06-2024 00:00",
    "description": "Treino de peito",
    "exercises": [
        {
            "description": "Spino com halteres",
            "loads": [{
                "repetitions": 10,
                "weight": 30
            }],
            "comment": "Até a fadiga."
        }
    ]
}
```
Response:

```
Status: 201 Created
```

- **Get Workout by ID**

```
GET /api/v1/workouts/{id}
```
Response Body:

```
{
    "start": "16/06/2023",
    "end": "17/06/2023 00:00",
    "description": "Treino de peito",
    "exercises": [
        {
            "description": "Supino com halteres",
            "loads": [{
                "repetitions": 10,
                "weight": 30
            }],
            "comment": "Até a fadiga."
        }
    ]
}
```

- **Get All Workouts**

```
GET /api/v1/workouts
```
Response Body:
```
[
  {
    "id": "your_id1",
    "description": "Treino de peito",
    "start": "16/06/2023",
    "end": "17/06/2023"
  },
  {
    "id": "your_id2",
    "description": "Treino de costas",
    "start": "18/06/2023",
    "end": "18/06/2023"
  }
]
```

- **Update Workout**

```
PUT /api/v1/workouts
```

Request Body:
```
{
    "id": "your_id",
    "description": "Treino de costas"
}
```
Response:

```
Status: 200 OK
```

- **Delete Workout**

```
DELETE /api/v1/workouts/{id}
```

Response:

```
Status: 200 OK
```

## License
This project is licensed under the MIT License. See the LICENSE file for details.

