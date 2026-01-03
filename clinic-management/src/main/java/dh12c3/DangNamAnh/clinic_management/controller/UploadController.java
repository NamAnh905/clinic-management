package dh12c3.DangNamAnh.clinic_management.controller;

import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);

            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Upload thành công")
                    .result(imageUrl)
                    .build();
        } catch (IOException e) {
            return ApiResponse.<String>builder()
                    .code(9999)
                    .message("Lỗi upload ảnh: " + e.getMessage())
                    .build();
        }
    }
}