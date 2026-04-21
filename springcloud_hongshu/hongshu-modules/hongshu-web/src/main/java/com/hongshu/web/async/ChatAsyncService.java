package com.hongshu.web.async;

import com.hongshu.system.api.RemoteChatService;
import com.hongshu.system.api.domain.LoginCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatAsyncService {

    @Autowired
    private RemoteChatService remoteChatService;


    @Async
    public void registerChatUser(String tel, String password) {
        Integer loginType = 3;
        LoginCommand loginCommand = new LoginCommand();
        loginCommand.setTel(tel);
        loginCommand.setPassword(password);
        loginCommand.setLoginType(loginType);
        remoteChatService.registerChatUser(loginCommand);
    }
}
