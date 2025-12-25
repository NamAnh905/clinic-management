package dh12c3.DangNamAnh.clinic_management.repository.medical;

import dh12c3.DangNamAnh.clinic_management.entity.medical.PrescriptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresDetailRepository extends JpaRepository<PrescriptionDetail, Long> {

    List<PrescriptionDetail> findByPrescription_PrescriptionId(Long prescriptionId);

    @Query("""
        SELECT COUNT(pd) > 0
        FROM PrescriptionDetail pd
        WHERE pd.prescription.medicalRecord.appointment.appointmentId = :appointmentId
        AND pd.drug.drugId = :drugId
    """)
    boolean isDrugInPrescription(@Param("appointmentId") Long appointmentId,
                                 @Param("drugId") Long drugId
    );
}
