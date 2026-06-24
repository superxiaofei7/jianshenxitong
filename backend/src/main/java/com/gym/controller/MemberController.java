package com.gym.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.R;
import com.gym.entity.Member;
import com.gym.entity.RechargeRecord;
import com.gym.mapper.RechargeRecordMapper;
import com.gym.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Page<Member>> list(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String keyword) {
        return R.ok(memberService.pageList(page, size, keyword));
    }

    @GetMapping("/{id}")
    public R<Member> getById(@PathVariable Long id) {
        return R.ok(memberService.getById(id));
    }

    @GetMapping("/byUser/{userId}")
    public R<Member> getByUserId(@PathVariable Long userId) {
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return R.fail("未找到会员信息");
        }
        return R.ok(member);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Member> save(@RequestBody Member member) {
        memberService.save(member);
        return R.ok(member);
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<Member> update(@RequestBody Member member) {
        memberService.updateById(member);
        return R.ok(member);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<?> delete(@PathVariable Long id) {
        memberService.removeById(id);
        return R.ok("删除成功");
    }

    @PostMapping("/{id}/recharge")
    @PreAuthorize("hasAnyAuthority('FRONT_DESK','FINANCE')")
    public R<?> recharge(@PathVariable Long id,
                          @RequestParam int hours,
                          @RequestParam(required = false) BigDecimal amount,
                          @RequestParam(required = false) String payMethod,
                          @RequestParam(required = false) String remark,
                          Authentication authentication) {
        Long operatorId = Long.valueOf(authentication.getPrincipal().toString());
        BigDecimal rechargeAmount = amount != null ? amount : BigDecimal.ZERO;

        // 记录充值
        RechargeRecord record = new RechargeRecord();
        record.setMemberId(id);
        record.setHours(hours);
        record.setAmount(rechargeAmount);
        record.setPayMethod(payMethod);
        record.setOperatorId(operatorId);
        record.setRemark(remark);
        rechargeRecordMapper.insert(record);

        // 更新会员课时和累计充值金额
        memberService.addHours(id, hours, rechargeAmount);
        return R.ok("充值成功");
    }

    @GetMapping("/stats/{id}")
    public R<Map<String, Object>> getStats(@PathVariable Long id) {
        return R.ok(memberService.getMemberStats(id));
    }
}
