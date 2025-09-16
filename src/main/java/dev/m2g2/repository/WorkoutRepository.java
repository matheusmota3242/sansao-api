package dev.m2g2.repository;

import dev.m2g2.model.Workout;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class WorkoutRepository {

    public static final String START = "start";
    public static final String END = "end";
    public static final String DATETIME_FORMAT = "%d/%m/%Y";
    public static final String WORKOUT = "workout";
    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    private final MongoTemplate mongoTemplate;

    public WorkoutRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void save(Workout workout) {
        mongoTemplate.save(workout);
    }

//    public List<WorkoutDTO> findAll() {
//        List<AggregationOperation> operations = new ArrayList<>();
//        operations.add(
//                Aggregation.project(START, END, DESCRIPTION, ID)
//                    .andExpression(START).dateAsFormattedString(DATETIME_FORMAT).as(START)
//                    .andExpression(END).dateAsFormattedString(DATETIME_FORMAT).as(END)
//        );
//        return mongoTemplate.aggregate(
//                Aggregation.newAggregation(operations),
//                WORKOUT,
//                WorkoutDTO.class).getMappedResults();
//    }

    public List<Workout> findAll() {
        return mongoTemplate.findAll(Workout.class, WORKOUT);
    }

    public Workout find(String id) {
        return mongoTemplate.findOne(Query.query(Criteria.where(ID).is(id)), Workout.class);
    }

    public List<Workout> findAllThatContains(String text) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("description").regex(text, "i"),
                Criteria.where("exercises.description").regex(text, "i"),
                Criteria.where("exercises.comment").regex(text, "i")
        );
        Query query = new Query(criteria) ;
        return mongoTemplate.find(query, Workout.class);
    }

    public void update(Workout workout) {
        Map<String, Object> fields = new HashMap<>();
        if (workout.getDescription() != null) {
            fields.put(DESCRIPTION, workout.getDescription());
        }

        if (workout.getStart() != null) {
            fields.put("start", workout.getStart());
        }

        if (workout.getEnd() != null) {
            fields.put("end", workout.getEnd());
        }

        if (workout.getExercises() != null) {
            fields.put("exercises", workout.getExercises());
        }

        if (fields.isEmpty())
            return;

        Query query = Query.query(Criteria.where(ID).is(workout.getId()));
        Update update = new Update();
        fields.forEach(update::set);
        mongoTemplate.updateFirst(query, update, Workout.class);
    }

    public void delete(String id) {
        Query query = Query.query(Criteria.where(ID).is(id));
        mongoTemplate.remove(query, Workout.class);
    }
}