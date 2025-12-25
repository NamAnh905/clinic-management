package dh12c3.DangNamAnh.clinic_management.service.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.SECreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.SEUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.ServiceEntityResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.ServiceEntity;
import dh12c3.DangNamAnh.clinic_management.enums.ServiceType;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.master.ServiceEntityMapper;
import dh12c3.DangNamAnh.clinic_management.repository.master.ServiceEntityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class SEService {
    ServiceEntityMapper serviceEntityMapper;
    ServiceEntityRepository serviceEntityRepository;

    @Transactional
    public ServiceEntityResponse create(SECreationRequest request){
        ServiceEntity serviceEntity = serviceEntityMapper.toEntity(request);

        ServiceEntity saved = serviceEntityRepository.save(serviceEntity);

        return serviceEntityMapper.toResponseEntity(saved);
    }

    @Transactional
    public ServiceEntityResponse update(SEUpdationRequest request, Long serviceId){
        ServiceEntity serviceEntity = serviceEntityRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        serviceEntityMapper.update(request,serviceEntity);

        ServiceEntity saved = serviceEntityRepository.save(serviceEntity);

        return serviceEntityMapper.toResponseEntity(saved);
    }

    public PageResponse<ServiceEntityResponse> findAll(ServiceType type, String keyword, int page, int size){
        Sort sort = Sort.by(Sort.Direction.ASC, "serviceId");
        Pageable  pageable = PageRequest.of(page - 1, size, sort);

        Page<ServiceEntity> serviceEntityPage = serviceEntityRepository.getAllServiceEntities(type, keyword, pageable);

        return serviceEntityMapper.toServicePage(serviceEntityPage);
    }

    public ServiceEntityResponse findById(Long serviceId){
        ServiceEntity serviceEntity = serviceEntityRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        return serviceEntityMapper.toResponseEntity(serviceEntity);
    }

    @Transactional
    public void delete(Long serviceId){
        ServiceEntity serviceEntity = serviceEntityRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        serviceEntity.setDeleted(true);
        serviceEntityRepository.save(serviceEntity);
    }
}
