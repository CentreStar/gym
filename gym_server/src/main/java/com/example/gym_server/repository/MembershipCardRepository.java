package com.example.gym_server.repository;
import com.example.gym_server.entity.MembershipCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MembershipCardRepository extends JpaRepository<MembershipCard, Long> { List<MembershipCard> findByEnabledTrueOrderByPriceAsc(); }
