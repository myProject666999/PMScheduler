package com.pm.scheduler.vo;

import lombok.Data;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private Integer enabled;
}
