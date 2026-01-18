package dh12c3.DangNamAnh.clinic_management.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Async // Chạy bất đồng bộ để không làm chậm quá trình đặt lịch
    public void sendBookingConfirmation(String toEmail, String fullName, String time, String doctorName, String username, String rawPassword) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đặt lịch khám thành công - 28Care System");

            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; padding: 20px;">
                    <h2 style="color: #007bff;">Đặt lịch thành công!</h2>
                    <p>Xin chào <b>%s</b>,</p>
                    <p>Cảm ơn bạn đã tin tưởng và đặt lịch tại 28Care. Dưới đây là thông tin lịch hẹn của bạn:</p>
                    <ul>
                        <li><b>Thời gian:</b> %s</li>
                        <li><b>Bác sĩ:</b> %s</li>
                    </ul>
                    <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                    
                    <h3>Thông tin tài khoản</h3>
                    <p>Hệ thống đã tạo cho bạn một tài khoản để theo dõi lịch sử khám bệnh:</p>
                    <div style="background: #f8f9fa; padding: 15px; border-radius: 5px;">
                        <p style="margin: 5px 0;"><b>Tài khoản (Email):</b> %s</p>
                        <p style="margin: 5px 0;"><b>Mật khẩu:</b> <span style="color: #d9534f; font-weight: bold;">%s</span></p>
                    </div>
                    <p><i>Vui lòng đổi mật khẩu sau khi đăng nhập để bảo mật thông tin.</i></p>
                    <br>
                    <p>Trân trọng,<br>Đội ngũ 28Care</p>
                </div>
                """, fullName, time, doctorName, username, rawPassword);

            helper.setText(htmlContent, true); // true = html
            javaMailSender.send(message);
            log.info("Email sent to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }

    // Hàm gửi email cho người dùng cũ (không gửi mật khẩu)
    @Async
    public void sendBookingNotification(String toEmail, String fullName, String time, String doctorName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đặt lịch khám - 28Care System");

            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05);">
                    <h2 style="color: #007bff; text-align: center; margin-bottom: 20px;">Đặt lịch thành công!</h2>
                    
                    <p>Xin chào <b>%s</b>,</p>
                    <p>Hệ thống 28Care xác nhận bạn đã đặt lịch hẹn mới thành công.</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 6px; margin: 15px 0; border-left: 4px solid #007bff;">
                        <ul style="list-style: none; padding: 0; margin: 0;">
                            <li style="margin-bottom: 10px;">🕒 <b>Thời gian:</b> <span style="font-size: 1.1em;">%s</span></li>
                            <li>👨‍⚕️ <b>Bác sĩ:</b> %s</li>
                        </ul>
                    </div>
                    
                    <p>Vui lòng đến đúng giờ để được phục vụ tốt nhất.</p>
                    
                    <hr style="border: 0; border-top: 1px solid #eee; margin: 25px 0;">
                    
                    <div style="text-align: center; font-size: 13px; color: #888;">
                        <p>Trân trọng,<br><b>Đội ngũ 28Care</b></p>
                        <p><i>Email này là tự động, vui lòng không trả lời.</i></p>
                    </div>
                </div>
                """, fullName, time, doctorName);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }
}