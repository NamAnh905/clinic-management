package dh12c3.DangNamAnh.clinic_management.service.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.DrugCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.DrugUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.DrugResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.Drug;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.master.DrugMapper;
import dh12c3.DangNamAnh.clinic_management.repository.master.DrugRepository;
import dh12c3.DangNamAnh.clinic_management.service.ExcelExportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class DrugService {

    DrugRepository drugRepository;
    DrugMapper drugMapper;
    ExcelExportService excelExportService;

    @Transactional
    public DrugResponse create(DrugCreationRequest request) {
        Drug drug = drugMapper.toDrug(request);
        Drug saved = drugRepository.save(drug);
        return drugMapper.toDrugResponse(saved);
    }

    @Transactional
    public DrugResponse update(DrugUpdateRequest request, Long drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new AppException(ErrorCode.DRUG_NOT_FOUND));

        drugMapper.update(request, drug);

        Drug saved = drugRepository.save(drug);
        return drugMapper.toDrugResponse(saved);
    }

    public PageResponse<DrugResponse> findAll(String keyword, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "drugId");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

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
    }

    public ByteArrayInputStream exportDrugs() throws IOException {
        List<Drug> drugs = drugRepository.findAll(Sort.by(Sort.Direction.ASC, "drugId"));

        List<DrugResponse> drugResponses = drugs.stream()
                .map(drugMapper::toDrugResponse)
                .toList();

        return excelExportService.exportToExcel(drugResponses, "Danh sách thuốc");
    }
}
