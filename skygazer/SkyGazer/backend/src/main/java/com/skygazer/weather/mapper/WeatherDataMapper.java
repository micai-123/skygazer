package com.skygazer.weather.mapper;

import com.skygazer.weather.entity.WeatherData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface WeatherDataMapper {

    long count();

    int insert(WeatherData weatherData);

    int insertBatch(@Param("list") List<WeatherData> list);

    int deleteAll();

    List<WeatherData> findRecentByLocation(@Param("location") String location,
                                           @Param("startTime") LocalDateTime startTime);

    Optional<WeatherData> findFirstByLocationOrderByRecordTimeDesc(String location);
}
