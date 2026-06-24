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
    public void sendBookingConfirmation(String toEmail, String fullName, String time, String doctorName,
            String specialtyName, String reason, String username, String rawPassword) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đặt lịch khám thành công - 28Care System");

            String htmlContent = String.format(
                    """
                    <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #eaeaea; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        <div style="background-color: #007bff; color: white; padding: 24px; text-align: center;">
                            <h2 style="margin: 0; font-size: 24px; font-weight: 600;">Xác nhận đặt lịch</h2>
                        </div>
                        <div style="padding: 32px 24px;">
                            <p style="font-size: 16px; color: #333; margin-top: 0;">Xin chào <b>%s</b>,</p>
                            <p style="font-size: 15px; color: #555; line-height: 1.6;">Cảm ơn bạn đã tin tưởng và đặt lịch tại 28Care. Chúng tôi đã nhận được yêu cầu và xác nhận lịch hẹn của bạn với thông tin chi tiết như sau:</p>
                            
                            <div style="background-color: #f8f9fa; border-radius: 6px; padding: 20px; margin: 24px 0;">
                                <table style="width: 100%%; border-collapse: collapse;">
                                    <tr>
                                        <td style="padding: 8px 0; color: #666; width: 120px;">Thời gian:</td>
                                        <td style="padding: 8px 0; font-weight: 600; color: #222;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;">Chuyên khoa:</td>
                                        <td style="padding: 8px 0; font-weight: 600; color: #222;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;">Bác sĩ:</td>
                                        <td style="padding: 8px 0; font-weight: 600; color: #222;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;">Lý do khám:</td>
                                        <td style="padding: 8px 0; font-weight: 600; color: #222;">%s</td>
                                    </tr>
                                </table>
                            </div>
                            
                            <h3 style="font-size: 16px; color: #333; margin-top: 32px; border-bottom: 1px solid #eaeaea; padding-bottom: 8px;">Thông tin tài khoản</h3>
                            <p style="font-size: 14px; color: #555; line-height: 1.6;">Hệ thống đã tự động tạo một tài khoản để bạn có thể quản lý lịch hẹn và xem hồ sơ bệnh án:</p>
                            <div style="background-color: #eef2f5; border-left: 4px solid #007bff; padding: 16px; margin: 16px 0; border-radius: 0 4px 4px 0;">
                                <p style="margin: 0 0 8px 0; font-size: 14px; color: #444;"><b>Tài khoản:</b> %s</p>
                                <p style="margin: 0; font-size: 14px; color: #444;"><b>Mật khẩu:</b> <span style="font-family: monospace; font-size: 16px; font-weight: bold; color: #007bff; letter-spacing: 1px;">%s</span></p>
                            </div>
                            <p style="font-size: 13px; color: #888; font-style: italic;">* Vui lòng đổi mật khẩu sau lần đăng nhập đầu tiên để bảo vệ tài khoản.</p>
                            
                            <div style="margin-top: 40px; text-align: center;">
                                <p style="font-size: 15px; color: #333; margin-bottom: 4px;">Trân trọng,</p>
                                <p style="font-size: 16px; font-weight: 600; color: #007bff; margin-top: 0;">Đội ngũ 28Care</p>
                            </div>
                        </div>
                        <div style="background-color: #f4f6f8; padding: 16px; text-align: center; font-size: 12px; color: #888;">
                            <p style="margin: 0;">Đây là email tự động, vui lòng không trả lời.</p>
                        </div>
                    </div>
                    """,
                    fullName, time, specialtyName, doctorName, reason, username, rawPassword);

            helper.setText(htmlContent, true); // true = html
            javaMailSender.send(message);
            log.info("Email sent to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }

    // Hàm gửi email cho người dùng cũ (không gửi mật khẩu)
    @Async
    public void sendBookingNotification(String toEmail, String fullName, String time, String doctorName, String specialtyName, String reason) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đặt lịch khám - 28Care System");

            String htmlContent = String.format(
                    """
                    <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #eaeaea; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        <div style="background-color: #007bff; color: white; padding: 24px; text-align: center;">
                            <h2 style="margin: 0; font-size: 24px; font-weight: 600;">Xác nhận đặt lịch</h2>
                        </div>
                        <div style="padding: 32px 24px;">
                            <p style="font-size: 16px; color: #333; margin-top: 0;">Xin chào <b>%s</b>,</p>
                            <p style="font-size: 15px; color: #555; line-height: 1.6;">Cảm ơn bạn đã tin tưởng và đặt lịch tại 28Care. Chúng tôi đã nhận được yêu cầu và xác nhận lịch hẹn của bạn với thông tin chi tiết như sau:</p>
                            
                            <div style="background-color: #f8f9fa; border-radius: 6px; padding: 20px; margin: 24px 0;">
                                <table style="width: 100%%; border-collapse: collapse;">
                                    <tr>
                                        <td style="padding: 8px 0; color: #666; width: 120px;">Thời gian:</td>
                                        <td style="padding: 8px 0; font-weight: 600; color: #222;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;">Chuyên khoa:</td>
                                        <td style="padding: 8px 0; font-weight: 600; color: #222;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;">Bác sĩ:</td>
                                        <td style="padding: 8px 0; font-weight: 600; color: #222;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;">Lý do khám:</td>
                                        <td style="padding: 8px 0; font-weight: 600; color: #222;">%s</td>
                                    </tr>
                                </table>
                            </div>
                            
                            <p style="font-size: 14px; color: #555; line-height: 1.6;">Vui lòng đến trước giờ khám 15 phút để hoàn tất thủ tục đăng ký.</p>
                            
                            <div style="margin-top: 40px; text-align: center;">
                                <p style="font-size: 15px; color: #333; margin-bottom: 4px;">Trân trọng,</p>
                                <p style="font-size: 16px; font-weight: 600; color: #007bff; margin-top: 0;">Đội ngũ 28Care</p>
                            </div>
                        </div>
                        <div style="background-color: #f4f6f8; padding: 16px; text-align: center; font-size: 12px; color: #888;">
                            <p style="margin: 0;">Đây là email tự động, vui lòng không trả lời.</p>
                        </div>
                    </div>
                    """,
                    fullName, time, specialtyName, doctorName, reason);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }

    @Async
    public void sendOtpResetPassword(String toEmail, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("Mã OTP khôi phục mật khẩu - 28Care");

            String htmlContent = String.format(
                    """
                            <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; border: 1px solid #eee; padding: 20px;">
                                <h2 style="color: #0ea5e9; text-align: center;">Khôi phục mật khẩu</h2>
                                <p>Chào bạn,</p>
                                <p>Bạn đã yêu cầu mã OTP để đặt lại mật khẩu cho tài khoản 28Care.</p>
                                <div style="background: #f0f9ff; padding: 15px; text-align: center; border-radius: 8px;">
                                    <span style="font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #0369a1;">%s</span>
                                </div>
                                <p style="color: #666; font-size: 13px; margin-top: 20px;">
                                    Mã này có hiệu lực trong vòng 5 phút. Nếu bạn không yêu cầu, vui lòng bỏ qua email này.
                                </p>
                            </div>
                            """,
                    otp);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("Lỗi gửi mail OTP: {}", e.getMessage());
        }
    }

    @Async
    public void sendCancellationNotification(String toEmail, String patientName, String time, String doctorName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("Thông báo hủy lịch hẹn - 28Care");

            String htmlContent = String.format(
                    """
                            <div style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05);">
                                <h2 style="color: #d9534f; text-align: center; margin-bottom: 20px;">Lịch hẹn đã bị hủy</h2>

                                <p>Xin chào <b>%s</b>,</p>
                                <p>Lịch hẹn sau đây của bạn tại 28Care đã được hủy:</p>

                                <div style="background-color: #fff3f3; padding: 15px; border-radius: 6px; margin: 15px 0; border-left: 4px solid #d9534f;">
                                    <ul style="list-style: none; padding: 0; margin: 0;">
                                        <li style="margin-bottom: 10px;">🕒 <b>Thời gian:</b> <span style="font-size: 1.1em;">%s</span></li>
                                        <li>👨‍⚕️ <b>Bác sĩ:</b> %s</li>
                                    </ul>
                                </div>

                                <p>Nếu bạn muốn đặt lại lịch khám, vui lòng truy cập hệ thống 28Care.</p>

                                <hr style="border: 0; border-top: 1px solid #eee; margin: 25px 0;">

                                <div style="text-align: center; font-size: 13px; color: #888;">
                                    <p>Trân trọng,<br><b>Đội ngũ 28Care</b></p>
                                    <p><i>Email này là tự động, vui lòng không trả lời.</i></p>
                                </div>
                            </div>
                            """,
                    patientName, time, doctorName);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            log.info("Cancellation email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send cancellation email", e);
        }
    }
}