package nba.studix.authservice.Client;

import nba.studix.authservice.DTO.LoginRequestDTO;
import nba.studix.authservice.DTO.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PostMapping("/api/users/validate-credentials")
    ResponseEntity<Map<String, Object>> validateCredentials(@RequestBody Map<String, String> request);

    @GetMapping("/api/users/email/{email}/info")
    ResponseEntity<UserInfoDTO> getUserInfoByEmail(@PathVariable String email);
}