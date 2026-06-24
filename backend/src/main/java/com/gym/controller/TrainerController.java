package com.gym.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.R;
import com.gym.entity.Trainer;
import com.gym.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    @Autowired
    private TrainerService trainerService;

    @GetMapping("/list")
    public R<Page<Trainer>> list(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String keyword) {
        return R.ok(trainerService.pageList(page, size, keyword));
    }

    @GetMapping("/available")
    public R<Page<Trainer>> available(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return R.ok(trainerService.availableList(page, size));
    }

    @GetMapping("/{id}")
    public R<Trainer> getById(@PathVariable Long id) {
        return R.ok(trainerService.getById(id));
    }

    @GetMapping("/byUser/{userId}")
    public R<Trainer> getByUserId(@PathVariable Long userId) {
        Trainer trainer = trainerService.getByUserId(userId);
        if (trainer == null) {
            return R.fail("未找到教练信息");
        }
        return R.ok(trainer);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Trainer> save(@RequestBody Trainer trainer) {
        trainerService.save(trainer);
        return R.ok(trainer);
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE','TRAINER')")
    public R<Trainer> update(@RequestBody Trainer trainer, Authentication authentication) {
        // 教练只能更新自己的信息
        String role = authentication.getAuthorities().stream()
                .findFirst().map(Object::toString).orElse("");
        if ("TRAINER".equals(role)) {
            Long userId = Long.valueOf(authentication.getPrincipal().toString());
            Trainer existing = trainerService.getByUserId(userId);
            if (existing == null || !existing.getId().equals(trainer.getId())) {
                return R.fail("只能修改自己的教练信息");
            }
        }
        trainerService.updateById(trainer);
        return R.ok(trainer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<?> delete(@PathVariable Long id) {
        trainerService.removeById(id);
        return R.ok("删除成功");
    }
}
