package com.gzpprod.center.config;

import com.gzpprod.center.common.UserRole;
import com.gzpprod.center.module.auth.entity.SysUser;
import com.gzpprod.center.module.auth.mapper.SysUserMapper;
import com.gzpprod.center.module.dispatch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DispatchService dispatchService;

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) == 0) {
            seed("dispatcher", UserRole.DISPATCHER, "调度员张三", "广州生产力促进中心");
            seed("auditor", UserRole.AUDITOR, "审核员李四", "广州生产力促进中心");
            seed("enterprise", UserRole.ENTERPRISE, "张明", "广州某某科技有限公司");
            seed("technician", UserRole.TECHNICIAN, "王工", "广州生产力促进中心中试基地");
        }
        dispatchService.seedResourcesIfEmpty();
    }

    private void seed(String username, UserRole role, String realName, String orgName) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(role.name());
        user.setRealName(realName);
        user.setOrgName(orgName);
        user.setPhone("13800138000");
        user.setEnabled(1);
        userMapper.insert(user);
    }
}
