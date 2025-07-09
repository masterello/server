package com.masterello.worker.dto;

import com.masterello.commons.core.validation.ErrorCodes;
import com.masterello.worker.validator.ServiceId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WorkerServiceDTO {
    @NotNull(message = ErrorCodes.SERVICE_ID_EMPTY)
    @ServiceId
    private Integer serviceId;
    @Max(value = 1000000,message = ErrorCodes.SERVICE_AMOUNT_MAX)
    private Integer amount;
    @Length(max = 255, message = ErrorCodes.SERVICES_DETAILS_MAX_LENGTH)
    private String details;
}
