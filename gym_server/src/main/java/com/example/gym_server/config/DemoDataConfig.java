package com.example.gym_server.config;

import com.example.gym_server.entity.GymCourse;
import com.example.gym_server.entity.MembershipCard;
import com.example.gym_server.entity.SystemConfig;
import com.example.gym_server.entity.User;
import com.example.gym_server.repository.GymCourseRepository;
import com.example.gym_server.repository.MembershipCardRepository;
import com.example.gym_server.repository.SystemConfigRepository;
import com.example.gym_server.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DemoDataConfig {
    @Bean
    CommandLineRunner seedDemoData(MembershipCardRepository cards, GymCourseRepository courses,
                                   UserRepository users, SystemConfigRepository configs,
                                   BCryptPasswordEncoder encoder) {
        return args -> {
            if (cards.count() == 0) {
                cards.save(card("月卡", 30, "199.00", "30 天自助训练权益"));
                cards.save(card("季卡", 90, "499.00", "适合稳定训练计划"));
                cards.save(card("年卡", 365, "1299.00", "全年健身与团课优惠"));
            }
            if (courses.count() == 0) {
                courses.save(course("燃脂操", "GROUP", "林教练", 1, 19, 0));
                courses.save(course("普拉提核心", "GROUP", "周教练", 2, 18, 30));
                courses.save(course("力量训练私教", "PT", "王教练", 3, 20, 0));
            }
            if (users.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setPhone("13800000000");
                admin.setRole("ADMIN");
                admin.setStatus("NORMAL");
                admin.setCreateTime(LocalDateTime.now());
                users.save(admin);
            }
            if (configs.count() == 0) {
                configs.save(config("max_booking_per_user", "3", "每个用户最大同时预约数"));
                configs.save(config("refund_deadline_days", "7", "退卡申请截止天数（购买后）"));
                configs.save(config("qr_code_expire_seconds", "300", "二维码有效期（秒）"));
                configs.save(config("private_lesson_price", "299.00", "私教课时单价"));
            }
        };
    }

    private MembershipCard card(String name, int days, String price, String description) {
        MembershipCard card = new MembershipCard();
        card.setName(name); card.setValidDays(days); card.setPrice(new BigDecimal(price)); card.setDescription(description);
        return card;
    }

    private GymCourse course(String title, String type, String coach, int plusDays, int hour, int minute) {
        GymCourse course = new GymCourse();
        course.setTitle(title); course.setType(type); course.setCoachName(coach);
        course.setCapacity(12); course.setPrice(new BigDecimal("39.00"));
        course.setStartTime(LocalDateTime.now().plusDays(plusDays).withHour(hour).withMinute(minute).withSecond(0).withNano(0));
        return course;
    }

    private SystemConfig config(String key, String value, String description) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key); config.setConfigValue(value); config.setDescription(description);
        return config;
    }
}
