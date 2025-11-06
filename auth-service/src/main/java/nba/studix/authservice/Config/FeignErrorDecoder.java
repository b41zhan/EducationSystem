package nba.studix.authservice.Config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        logger.error("Feign client error: MethodKey: {}, Status: {}, Reason: {}",
                methodKey, response.status(), response.reason());

        switch (response.status()) {
            case 400:
                return new RuntimeException("Bad Request from user-service");
            case 404:
                return new RuntimeException("User service not found");
            case 500:
                return new RuntimeException("Internal server error in user-service");
            default:
                return new RuntimeException("Error in user-service: " + response.status());
        }
    }
}