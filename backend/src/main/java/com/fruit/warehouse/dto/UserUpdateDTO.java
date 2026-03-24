package com.fruit.warehouse.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private List<Long> roleIds;
}
