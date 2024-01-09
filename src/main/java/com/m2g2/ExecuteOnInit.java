package com.m2g2;

import com.m2g2.enums.MassUnity;
import com.m2g2.model.Load;
import com.m2g2.model.Training;
import com.m2g2.model.TrainingItem;
import com.m2g2.model.TrainingType;
import com.m2g2.repository.TrainingTypeRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class ExecuteOnInit implements InitializingBean {

    private final TrainingTypeRepository repository;

    public ExecuteOnInit(TrainingTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        TrainingType trainingType = new TrainingType();
        trainingType.setName("Treino de Peito");
        trainingType.setTrainings(getTrainings());
        repository.save(trainingType);
    }

    private List<Training> getTrainings() {
        Training training = new Training();
        training.setStartTime(LocalDateTime.now());
        training.setEndTime(LocalDateTime.now());
        training.setTrainingItens(getTrainingItens());
        return Arrays.asList(training);
    }

    private List<TrainingItem> getTrainingItens() {
        TrainingItem trainingItem = new TrainingItem();
        trainingItem.setLoads(getLoadings());
        return Arrays.asList(trainingItem);
    }

    private List<Load> getLoadings() {
        Load load = new Load();
        load.setQuantity(10);
        load.setUnity(MassUnity.KG);
        load.setWeight(100f);
        return Arrays.asList(load);
    }
}
