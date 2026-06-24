package com.gym.service;

import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getDashboardStats();
    Map<String, Object> getMonthlyRevenue(int year, int month);
    Map<String, Object> getTrainerStats(Long trainerId);
    Map<String, Object> getMemberActivity();
    Map<String, Object> getFinanceStats();
}
