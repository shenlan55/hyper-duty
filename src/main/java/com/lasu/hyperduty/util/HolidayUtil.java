package com.lasu.hyperduty.util;

import com.lasu.hyperduty.entity.DutyHoliday;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 节假日工具类
 * 用于获取节假日信息
 */
public class HolidayUtil {

    /**
     * 获取指定年份的节假日信息
     * @param year 年份
     * @return 节假日列表
     */
    public static List<DutyHoliday> getHolidays(int year) {
        List<DutyHoliday> holidays = new ArrayList<>();
        
        try {
            // 使用真实的节假日API接口
            String url = "https://timor.tech/api/holiday/year/" + year;
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                
                // 解析API返回的数据
                Integer code = (Integer) result.get("code");
                if (code != null && code == 0) {
                    Map<String, Object> holidayData = (Map<String, Object>) result.get("holiday");
                    if (holidayData != null) {
                        // 遍历所有节假日数据
                        for (Map.Entry<String, Object> entry : holidayData.entrySet()) {
                            String dateKey = entry.getKey(); // 格式：MM-DD
                            Map<String, Object> holidayInfo = (Map<String, Object>) entry.getValue();
                            
                            // 构建完整日期字符串：YYYY-MM-DD
                            String dateStr = year + "-" + dateKey;
                            Boolean isHoliday = (Boolean) holidayInfo.get("holiday");
                            String name = (String) holidayInfo.get("name");
                            
                            // 创建节假日对象
                            DutyHoliday holiday = new DutyHoliday();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            LocalDate date = LocalDate.parse(dateStr, formatter);
                            
                            holiday.setHolidayName(name);
                            holiday.setHolidayDate(date);
                            holiday.setIsWorkday(isHoliday ? 0 : 1); // 0-节假日，1-工作日（调休）
                            holiday.setHolidayType(isHoliday ? 1 : 2); // 1-法定节假日，2-调休
                            holiday.setRemark(name);
                            holiday.setCreateTime(java.time.LocalDateTime.now());
                            holiday.setUpdateTime(java.time.LocalDateTime.now());
                            
                            holidays.add(holiday);
                        }
                    }
                }
            } else {
                // API调用成功但无数据，返回空列表
                System.err.println("API调用成功但无数据返回");
                return holidays;
            }
        } catch (Exception e) {
            // 当API调用失败时，直接返回空列表
            // 不要使用默认数据，获取失败就是空
            System.err.println("获取节假日信息失败: " + e.getMessage());
        }
        
        return holidays;
    }
    
    /**
     * 检查指定日期是否为节假日
     * @param date 日期
     * @param holidays 节假日列表
     * @return 是否为节假日
     */
    public static boolean isHoliday(LocalDate date, List<DutyHoliday> holidays) {
        for (DutyHoliday holiday : holidays) {
            if (holiday.getHolidayDate().equals(date) && holiday.getIsWorkday() == 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查指定日期是否为工作日（包括调休）
     * @param date 日期
     * @param holidays 节假日列表
     * @return 是否为工作日
     */
    public static boolean isWorkday(LocalDate date, List<DutyHoliday> holidays) {
        // 先检查是否为周末
        int dayOfWeek = date.getDayOfWeek().getValue();
        boolean isWeekend = dayOfWeek == 6 || dayOfWeek == 7;
        
        // 检查是否在节假日列表中
        for (DutyHoliday holiday : holidays) {
            if (holiday.getHolidayDate().equals(date)) {
                return holiday.getIsWorkday() == 1;
            }
        }
        
        // 非节假日的周末不是工作日，非周末是工作日
        return !isWeekend;
    }
}
