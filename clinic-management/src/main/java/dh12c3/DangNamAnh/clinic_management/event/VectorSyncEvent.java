package dh12c3.DangNamAnh.clinic_management.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VectorSyncEvent {
    private String type;
    private String action;
    private Long id;
    private Object payload;
}