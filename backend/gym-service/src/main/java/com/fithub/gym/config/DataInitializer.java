package com.fithub.gym.config;

import com.fithub.gym.entity.ClassType;
import com.fithub.gym.entity.Equipment;
import com.fithub.gym.entity.FitnessClass;
import com.fithub.gym.entity.Location;
import com.fithub.gym.entity.Trainer;
import com.fithub.gym.entity.TrainingRoom;
import com.fithub.gym.repository.ClassTypeRepository;
import com.fithub.gym.repository.EquipmentRepository;
import com.fithub.gym.repository.FitnessClassRepository;
import com.fithub.gym.repository.LocationRepository;
import com.fithub.gym.repository.TrainerRepository;
import com.fithub.gym.repository.TrainingRoomRepository;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedGymData(
        LocationRepository locations,
        TrainingRoomRepository rooms,
        TrainerRepository trainers,
        ClassTypeRepository classTypes,
        EquipmentRepository equipmentRepository,
        FitnessClassRepository classes
    ) {
        return args -> {
            if (locations.count() > 0) {
                return;
            }

            Location location = new Location();
            location.setName("FitHub Central");
            location.setAddress("Strada Sportului 10");
            location.setCity("Bucuresti");
            location = locations.save(location);

            Equipment bike = new Equipment();
            bike.setName("Bicicleta indoor");
            bike.setDescription("Bicicleta pentru clase cardio.");
            bike = equipmentRepository.save(bike);

            TrainingRoom room = new TrainingRoom();
            room.setLocation(location);
            room.setName("Sala Cardio");
            room.setCapacity(20);
            room.setEquipment(Set.of(bike));
            room = rooms.save(room);

            Trainer trainer = new Trainer();
            trainer.setFirstName("Mara");
            trainer.setLastName("Ionescu");
            trainer.setEmail("mara.ionescu@fithub.local");
            trainer.setSpecialization("HIIT");
            trainer = trainers.save(trainer);

            ClassType type = new ClassType();
            type.setName("HIIT");
            type.setDescription("Antrenament intens pe intervale.");
            type.setDifficultyLevel("MEDIUM");
            type = classTypes.save(type);

            FitnessClass fitnessClass = new FitnessClass();
            fitnessClass.setName("HIIT Morning");
            fitnessClass.setClassType(type);
            fitnessClass.setTrainer(trainer);
            fitnessClass.setTrainingRoom(room);
            fitnessClass.setStartTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(0));
            fitnessClass.setEndTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));
            fitnessClass.setCapacity(20);
            fitnessClass.setAvailableSlots(20);
            fitnessClass.setStatus("SCHEDULED");
            classes.save(fitnessClass);
        };
    }
}
