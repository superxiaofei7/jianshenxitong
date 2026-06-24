package com.gym.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Course;

public interface CourseService extends IService<Course> {
    Page<Course> pageList(int page, int size, String keyword, String courseType);
    Page<Course> availableCourses(int page, int size);
}
