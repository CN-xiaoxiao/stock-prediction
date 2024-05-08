package com.xiaoxiao.stockbackend.controller;

import com.xiaoxiao.stockbackend.entity.RestBean;
import com.xiaoxiao.stockbackend.entity.vo.request.ChangePasswordVO;
import com.xiaoxiao.stockbackend.entity.vo.request.ModifyEmailVO;
import com.xiaoxiao.stockbackend.service.AuthorizeService;
import com.xiaoxiao.stockbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName UserController
 * @Description 用户操作控制层
 * @Author xiaoxiao
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    UserService userService;

    @PostMapping("/change-password")
    public RestBean<Void> changePassword(@RequestBody @Valid ChangePasswordVO vo,
                                         @RequestAttribute("id") int id) {
        return userService.changePassword(id, vo.getPassword(), vo.getNew_password()) ?
                RestBean.success() : RestBean.failure(401, "原密码输入错误");
    }

    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(@RequestAttribute("id") int id,
                                      @RequestBody @Valid ModifyEmailVO vo) {
        String result = userService.modifyEmail(id, vo);

        if (result == null) {
            return RestBean.success();
        } else {
            return RestBean.failure(401, result);
        }
    }
}
