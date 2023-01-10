
package com.example.manmu.controller;
import com.example.manmu.config.auth.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class UserInfoController {

    private final HttpSession httpSession;

    @GetMapping("/api/userInfo")
    public Map<String, Object> UserInfo() {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if(user != null){
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", user.getName());
            userInfo.put("email",user.getEmail());
            userInfo.put("picture",user.getPicture());
            return userInfo;
        }else{
            throw new RuntimeException("user not found in session");
        }
    }

}
