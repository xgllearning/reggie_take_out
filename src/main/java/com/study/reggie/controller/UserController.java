package com.study.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.study.reggie.common.R;
import com.study.reggie.entity.User;
import com.study.reggie.service.UserService;
import com.study.reggie.utils.SMSUtils;
import com.study.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);

            //需要将生成的验证码保存到Session,key为用户手机号，value为验证码
            session.setAttribute(phone,code);

            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String,String> map, HttpSession session){
        log.info("map:{}",map);
        //解析前台传过来的数据，获取手机号和验证码
        String phone = map.get("phone");
        String code = map.get("code");
        //对手机号和验证码进行验证，判断是否正确
        String sessionCode = (String) session.getAttribute(phone);
        //进行验证码比对(页面提交的验证码和Session中保存的验证码比对)
        if (sessionCode!=null&&sessionCode.equalsIgnoreCase(code)){
            //比对成功，可以登录
            //登录前需要判断数据库是否存在该用户，如果不存在则直接创建该用户
            LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class);
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user==null){
                //说明该数据库无该用户，自动创建
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //此时说明用户登录成功，需要把该用户Id存储到session中，让过滤器放行
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }

}
