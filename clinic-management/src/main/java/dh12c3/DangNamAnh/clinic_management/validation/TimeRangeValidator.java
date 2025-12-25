package dh12c3.DangNamAnh.clinic_management.validation;

import dh12c3.DangNamAnh.clinic_management.dto.request.schedule.ScheduleCreationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, ScheduleCreationRequest> {

    @Override
    public boolean isValid(ScheduleCreationRequest request, ConstraintValidatorContext context){
        if(request.getStartTime() == null || request.getEndTime() == null){
            return true;
        }
        return request.getEndTime().isAfter(request.getStartTime());
    }
}
