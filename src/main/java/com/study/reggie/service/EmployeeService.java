package com.study.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.reggie.common.R;
import com.study.reggie.entity.Employee;

public interface EmployeeService extends IService<Employee> {
    R login(Employee employee);

}
