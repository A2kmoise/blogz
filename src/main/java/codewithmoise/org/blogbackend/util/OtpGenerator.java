package codewithmoise.org.blogbackend.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {
    public String generateOtp(){
        SecureRandom secureRandom = new SecureRandom();

        int randomNumber = secureRandom.nextInt(900000);

        int otp = 100000+randomNumber;

        return String.valueOf(otp);
    }
}
