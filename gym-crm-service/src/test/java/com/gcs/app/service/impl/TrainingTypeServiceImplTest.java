package com.gcs.app.service.impl;

import com.gcs.app.facade.dto.TrainingTypeResponseDto;
import com.gcs.app.mapper.TrainingTypeMapper;
import com.gcs.app.model.TrainingType;
import com.gcs.app.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

    private static final String TRAINING_NAME_YOGA = "Yoga";
    private static final String TRAINING_NAME_PILATES = "Pilates";

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @InjectMocks
    private TrainingTypeServiceImpl service;

    @Test
    void getAll_returnsListOfTrainingTypeResponseDto() {
        TrainingType trainingType1 = TrainingType.builder().id(1L).name(TRAINING_NAME_YOGA).build();
        TrainingType trainingType2 = TrainingType.builder().id(2L).name(TRAINING_NAME_PILATES).build();

        List<TrainingType> trainingTypes = of(trainingType1, trainingType2);

        List<TrainingTypeResponseDto> expected = List.of(
                new TrainingTypeResponseDto(1L, TRAINING_NAME_YOGA),
                new TrainingTypeResponseDto(2L, TRAINING_NAME_PILATES)
        );

        when(trainingTypeRepository.findAll()).thenReturn(trainingTypes);
        when(trainingTypeMapper.toDtoList(trainingTypes)).thenReturn(expected);

        List<TrainingTypeResponseDto> actual = service.getAll();

        assertEquals(expected, actual);

        verify(trainingTypeRepository).findAll();
        verify(trainingTypeMapper).toDtoList(trainingTypes);
        verifyNoMoreInteractions(trainingTypeRepository, trainingTypeMapper);
    }
}