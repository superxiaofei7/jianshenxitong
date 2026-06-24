package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Course;
import com.gym.mapper.CourseMapper;
import com.gym.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    public Page<Course> pageList(int page, int size, String keyword, String courseType) {
        Page<Course> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getCourseName, keyword);
        }
        if (courseType != null && !courseType.isEmpty()) {
            wrapper.eq(Course::getCourseType, courseType);
        }
        wrapper.orderByDesc(Course::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public Page<Course> availableCourses(int page, int size) {
        Page<Course> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1);
        wrapper.orderByDesc(Course::getCreateTime);
        return page(pageParam, wrapper);
    }
}
