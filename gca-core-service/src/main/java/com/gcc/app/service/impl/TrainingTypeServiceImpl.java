package com.gcc.app.service.impl;

import com.gcc.app.exception.ServiceException;
import com.gcc.app.facade.dto.TrainingTypeResponseDto;
import com.gcc.app.mapper.TrainingTypeMapper;
import com.gcc.app.model.TrainingType;
import com.gcc.app.repository.TrainingTypeRepository;
import com.gcc.app.service.TrainingTypeService;
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
