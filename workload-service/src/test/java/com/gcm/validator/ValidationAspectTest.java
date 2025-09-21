package com.gcm.validator;

import com.gcm.service.dto.TrainerWorkloadRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ValidationAspectTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private ValidationAspect aspect;

    private TrainerWorkloadRequestDto validDto = createValidDto();
    private TrainerWorkloadRequestDto invalidDto = createInvalidDto();

    @Test
    void shouldNotThrowExceptionForValidDto() {
        doReturn(Collections.emptySet()).when(validator).validate(any());

        aspect.validateMessage(null, validDto);
    }

    @Test
    void shouldThrowExceptionForInvalidDto() {
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        Set<ConstraintViolation<Object>> violations = Collections.singleton(violation);

        doReturn(violations).when(validator).validate(any());

        assertThrows(ConstraintViolationException.class,
                () -> aspect.validateMessage(null, invalidDto));
    }

    private TrainerWorkloadRequestDto createValidDto() {
        return TrainerWorkloadRequestDto.builder()
                .username("tom.cruise")
                .firstName("Tom")
                .lastName("Cruise")
                .active(true)
                .trainingDate(LocalDate.now())
                .durationInMinutes(60L)
                .actionType(TrainerWorkloadRequestDto.ActionType.ADD)
                .build();
    }

    private TrainerWorkloadRequestDto createInvalidDto() {
        return TrainerWorkloadRequestDto.builder()
                .username("")
                .firstName("Tom")
                .lastName("Cruise")
                .active(true)
                .trainingDate(LocalDate.now().plusDays(1))
                .durationInMinutes(-5L)
                .actionType(null)
                .build();
    }
}