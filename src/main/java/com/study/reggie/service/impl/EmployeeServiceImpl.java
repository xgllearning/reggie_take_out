package com.study.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.reggie.common.R;
import com.study.reggie.entity.Employee;
import com.study.reggie.mapper.EmployeeMapper;
import com.study.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Override
    public R login(Employee employee) {
        //①. 先获取用户输入的密码，然后通过工具类将页面提交的密码password进行md5加密处理, 得到加密后的字符串
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //②. 根据页面提交的用户名username查询数据库中员工数据信息
        LambdaQueryWrapper<Employee> queryWrapper = Wrappers.lambdaQuery(Employee.class);
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = baseMapper.selectOne(queryWrapper);
        //③. 因为userName字段为唯一，所以查询到则为对象，如果没有查询到为null, 则返回登录失败结果
        if (emp==null){
            return R.error("查询失败,用户名不匹配");
        }
        //④. 密码比对，如果不一致, 则返回登录失败结果
        if (!password.equals(emp.getPassword())){
            return R.error("查询失败,密码不匹配");
        }
        //⑤. 查看员工状态，如果为0=已禁用状态，则返回员工已禁用结果
        if (emp.getStatus()==0){
            return R.error("查询失败,status=0");
        }
        //⑥. 登录成功，将员工id存入Session, 并返回登录成功结果
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession();
        session.setAttribute("employee",emp.getId());
        return R.success(emp);
    }
}
