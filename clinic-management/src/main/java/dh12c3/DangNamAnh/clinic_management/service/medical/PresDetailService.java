package dh12c3.DangNamAnh.clinic_management.service.medical;

import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PresDetailCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PresDetailUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.medical.PresDetailResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.Drug;
import dh12c3.DangNamAnh.clinic_management.entity.medical.Prescription;
import dh12c3.DangNamAnh.clinic_management.entity.medical.PrescriptionDetail;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.medical.PresDetailMapper;
import dh12c3.DangNamAnh.clinic_management.repository.master.DrugRepository;
import dh12c3.DangNamAnh.clinic_management.repository.medical.PresDetailRepository;
import dh12c3.DangNamAnh.clinic_management.repository.medical.PrescriptionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class PresDetailService {
    PresDetailMapper presDetailMapper;
    PresDetailRepository presDetailRepository;
    DrugRepository drugRepository;
    PrescriptionRepository prescriptionRepository;

    @Transactional
    public PresDetailResponse create(PresDetailCreationRequest request){
        Prescription prescription = prescriptionRepository.findById(request.getPrescriptionId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        Drug drug = drugRepository.findById(request.getDrugId())
                .orElseThrow(() -> new AppException(ErrorCode.DRUG_NOT_FOUND));

        PrescriptionDetail prescriptionDetail = presDetailMapper.toPrescriptionDetail(request);
        prescriptionDetail.setPrescription(prescription);
        prescriptionDetail.setDrug(drug);

        presDetailRepository.save(prescriptionDetail);
        return presDetailMapper.toPresDetailResponse(prescriptionDetail);
    }

    @Transactional
    public PresDetailResponse update(PresDetailUpdateRequest request, Long detailId){
        PrescriptionDetail detail = presDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.DETAIL_NOT_FOUND));

        presDetailMapper.update(request,detail);

        return presDetailMapper.toPresDetailResponse(detail);
    }

    public List<PresDetailResponse> findDetailsByPrescriptionId(Long prescriptionId) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            throw new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND);
        }

        List<PrescriptionDetail> details = presDetailRepository.findByPrescription_PrescriptionId(prescriptionId);
        return details.stream()
                .map(presDetailMapper::toPresDetailResponse)
                .toList();
    }

    public PresDetailResponse findById(Long detailId){
        PrescriptionDetail detail = presDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.DETAIL_NOT_FOUND));

        return presDetailMapper.toPresDetailResponse(detail);
    }

    @Transactional
    public void deleteById(Long detailId){
        presDetailRepository.deleteById(detailId);
    }
}
