package dh12c3.DangNamAnh.clinic_management.service.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.DrugCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.DrugUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.DrugResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.Drug;
import dh12c3.DangNamAnh.clinic_management.event.VectorSyncEvent;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.master.DrugMapper;
import dh12c3.DangNamAnh.clinic_management.repository.master.DrugRepository;
import dh12c3.DangNamAnh.clinic_management.service.ExcelExportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class DrugService {

    DrugRepository drugRepository;
    DrugMapper drugMapper;
    ExcelExportService excelExportService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public DrugResponse create(DrugCreationRequest request) {
        Drug drug = drugMapper.toDrug(request);
        Drug saved = drugRepository.save(drug);

        DrugResponse response = drugMapper.toDrugResponse(saved);
        eventPublisher.publishEvent(new VectorSyncEvent("service", "UPDATE", response.getDrugId(), response));

        return response;
    }

    @Transactional
    public DrugResponse update(DrugUpdateRequest request, Long drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new AppException(ErrorCode.DRUG_NOT_FOUND));

        drugMapper.update(request, drug);

        Drug saved = drugRepository.save(drug);

        DrugResponse response = drugMapper.toDrugResponse(saved);
        eventPublisher.publishEvent(new VectorSyncEvent("service", "UPDATE", response.getDrugId(), response));

        return response;
    }

    public PageResponse<DrugResponse> findAll(String keyword, int page, int size, String sortBy, String sortDir) {
        String sortField = SORT_MAPPING.getOrDefault(sortBy, "stockQuantity");
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));

        Page<Drug> drugs = drugRepository.getAllDrugs(keyword, pageable);

        return drugMapper.toDrugPage(drugs);
    }

    public DrugResponse findById(Long drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new AppException(ErrorCode.DRUG_NOT_FOUND));

        return drugMapper.toDrugResponse(drug);
    }

    @Transactional
    public void delete(Long drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new AppException(ErrorCode.DRUG_NOT_FOUND));

        drug.setDeleted(true);
        drugRepository.save(drug);

        eventPublisher.publishEvent(new VectorSyncEvent("drug", "DELETE", drugId, null));
    }

    public ByteArrayInputStream exportDrugs() throws IOException {
        List<Drug> drugs = drugRepository.findAll(Sort.by(Sort.Direction.ASC, "drugId"));

        List<DrugResponse> drugResponses = drugs.stream()
                .map(drugMapper::toDrugResponse)
                .toList();

        return excelExportService.exportToExcel(drugResponses, "Danh sách thuốc");
    }

    Map<String, String> SORT_MAPPING = Map.of(
            "name", "name",
            "stockQuantity", "stockQuantity",
            "price", "price"
    );
}
