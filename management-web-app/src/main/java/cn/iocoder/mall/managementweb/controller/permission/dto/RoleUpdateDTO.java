package cn.iocoder.mall.managementweb.controller.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("角色更新 DTO")
@Data
public class RoleUpdateDTO {

    @ApiModelProperty(value = "角色编号", required = true, example = "1")
    @NotNull(message = "角色编号不能为空")
    private Integer id;
    @ApiModelProperty(value = "角色名", required = true, example = "管理员")
    @NotEmpty(message = "角色名不能为空")
    private String name;
    @ApiModelProperty(value = "角色编码", example = "ADMIN")
    private String code;

}
