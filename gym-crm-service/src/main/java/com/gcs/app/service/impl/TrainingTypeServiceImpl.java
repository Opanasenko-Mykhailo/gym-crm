package com.gcs.app.service.impl;

import com.gcs.app.exception.ServiceException;
import com.gcs.app.facade.dto.TrainingTypeResponseDto;
import com.gcs.app.mapper.TrainingTypeMapper;
import com.gcs.app.model.TrainingType;
import com.gcs.app.repository.TrainingTypeRepository;
import com.gcs.app.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingTypeMapper trainingTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TrainingTypeResponseDto> getAll() {
        log.info("Fetching all training types");
        List<TrainingType> trainingTypes = trainingTypeRepository.findAll();

        return trainingTypeMapper.toDtoList(trainingTypes);
    }

    @Override
    public TrainingType getByName(String trainingTypeName) {
        log.info("Fetching TrainingType by name: {}", trainingTypeName);

        return trainingTypeRepository.findByName(trainingTypeName)
                .orElseThrow(() -> new ServiceException(String.format("TrainingType not found with name: %s", trainingTypeName)));
    }
}
