package com.eflo.user.mapper;

import com.eflo.user.domain.dto.ActivityLogDTO;
import com.eflo.user.domain.entity.UserActivityLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    ActivityLogDTO toDTO(UserActivityLog activityLog);

    List<ActivityLogDTO> toDTOList(List<UserActivityLog> activityLogs);
}
