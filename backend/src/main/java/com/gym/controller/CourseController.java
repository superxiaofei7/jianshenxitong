package com.gym.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.R;
import com.gym.entity.Course;
import com.gym.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/list")
    public R<Page<Course>> list(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String courseType) {
        return R.ok(courseService.pageList(page, size, keyword, courseType));
    }

    @GetMapping("/available")
    public R<Page<Course>> available(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return R.ok(courseService.availableCourses(page, size));
    }

    @GetMapping("/{id}")
    public R<Course> getById(@PathVariable Long id) {
        return R.ok(courseService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Course> save(@RequestBody Course course) {
        courseService.save(course);
        return R.ok(course);
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Course> update(@RequestBody Course course) {
        courseService.updateById(course);
        return R.ok(course);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<?> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return R.ok("删除成功");
    }
}
