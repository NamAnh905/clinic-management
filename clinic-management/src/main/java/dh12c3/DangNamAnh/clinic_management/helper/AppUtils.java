package dh12c3.DangNamAnh.clinic_management.helper;

import java.util.Random;

public class AppUtils {
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public static String generateEmployeeCode() {
        Random rand = new Random();
        int number = rand.nextInt(1000);

        return String.format("NV%03d", number);
    }

    public static String generateLicenseNumber() {
        StringBuilder sb = new StringBuilder("VN-");
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }

    public static String generateTransactionCode() {
        String timeStamp = new java.text.SimpleDateFormat("yyMMddHHmm").format(new java.util.Date());
        String randomStr = generateRandomString(4);
        return "TRX" + timeStamp + randomStr;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }
}
