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
            // 这里使用国务院办公厅发布的节假日安排API
            // 实际项目中可能需要替换为真实的API接口
            String url = "https://api.example.com/holidays?year=" + year;
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                // 解析API返回的数据，转换为DutyHoliday对象
                // 这里需要根据实际API返回格式进行调整
                // 示例代码，实际实现需要根据真实API进行修改
            }
        } catch (Exception e) {
            // 当API调用失败时，使用默认节假日数据
            holidays.addAll(getDefaultHolidays(year));
        }
        
        return holidays;
    }
    
    /**
     * 获取默认节假日数据
     * 当API调用失败时使用
     * @param year 年份
     * @return 节假日列表
     */
    private static List<DutyHoliday> getDefaultHolidays(int year) {
        List<DutyHoliday> holidays = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 元旦
        holidays.add(createHoliday(year + "-01-01", "元旦", 0, 1));
        
        // 春节
        holidays.add(createHoliday(year + "-02-10", "春节", 0, 1));
        holidays.add(createHoliday(year + "-02-11", "春节", 0, 1));
        holidays.add(createHoliday(year + "-02-12", "春节", 0, 1));
        holidays.add(createHoliday(year + "-02-13", "春节", 0, 1));
        holidays.add(createHoliday(year + "-02-14", "春节", 0, 1));
        
        // 清明节
        holidays.add(createHoliday(year + "-04-04", "清明节", 0, 1));
        
        // 劳动节
        holidays.add(createHoliday(year + "-05-01", "劳动节", 0, 1));
        
        // 端午节
        holidays.add(createHoliday(year + "-06-10", "端午节", 0, 1));
        
        // 中秋节
        holidays.add(createHoliday(year + "-09-17", "中秋节", 0, 1));
        
        // 国庆节
        holidays.add(createHoliday(year + "-10-01", "国庆节", 0, 1));
        holidays.add(createHoliday(year + "-10-02", "国庆节", 0, 1));
        holidays.add(createHoliday(year + "-10-03", "国庆节", 0, 1));
        
        return holidays;
    }
    
    /**
     * 创建节假日对象
     * @param dateStr 日期字符串
     * @param name 节假日名称
     * @param isWorkday 是否工作日（0-否，1-是）
     * @param holidayType 节假日类型（1-法定节假日，2-调休）
     * @return 节假日对象
     */
    private static DutyHoliday createHoliday(String dateStr, String name, int isWorkday, int holidayType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, formatter);
        
        DutyHoliday holiday = new DutyHoliday();
        holiday.setHolidayName(name);
        holiday.setHolidayDate(date);
        holiday.setIsWorkday(isWorkday);
        holiday.setHolidayType(holidayType);
        holiday.setRemark(name);
        holiday.setCreateTime(java.time.LocalDateTime.now());
        holiday.setUpdateTime(java.time.LocalDateTime.now());
        
        return holiday;
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
