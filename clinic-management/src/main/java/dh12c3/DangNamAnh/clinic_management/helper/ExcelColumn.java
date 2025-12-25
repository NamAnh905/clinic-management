package dh12c3.DangNamAnh.clinic_management.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // Annotation tồn tại lúc chạy chương trình
// Quan trọng: Target này cho phép gắn annotation lên cả BIẾN (FIELD) và HÀM (METHOD)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ExcelColumn {
    String name();
    int width() default 5000;
}
