package cn.iocoder.mall.system.rest.controller.authorization;

import cn.iocoder.common.framework.enums.MallConstants;
import cn.iocoder.common.framework.vo.CommonResult;
import cn.iocoder.mall.security.core.annotation.RequiresPermissions;
import cn.iocoder.mall.security.core.context.AdminSecurityContextHolder;
import cn.iocoder.mall.system.biz.bo.authorization.ResourceBO;
import cn.iocoder.mall.system.biz.bo.authorization.ResourceTreeNodeBO;
import cn.iocoder.mall.system.biz.dto.authorization.AuthorizationAssignRoleResourceDTO;
import cn.iocoder.mall.system.biz.dto.authorization.AuthorizationGetResourcesByAccountIdDTO;
import cn.iocoder.mall.system.biz.dto.authorization.AuthorizationGetRoleResourcesDTO;
import cn.iocoder.mall.system.biz.dto.authorization.ResourceGetTreeDTO;
import cn.iocoder.mall.system.biz.enums.authorization.ResourceTypeEnum;
import cn.iocoder.mall.system.biz.service.authorization.AuthorizationService;
import cn.iocoder.mall.system.biz.service.authorization.ResourceService;
import cn.iocoder.mall.system.biz.service.authorization.RoleService;
import cn.iocoder.mall.system.rest.convert.authorization.AdminsAuthorizationConvert;
import cn.iocoder.mall.system.rest.request.authorization.AdminsAuthorizationAssignRoleResourceRequest;
import cn.iocoder.mall.system.rest.response.authorization.AdminsAuthorizationMenuTreeResponse;
import cn.iocoder.mall.system.rest.response.authorization.AdminsAuthorizationRoleResourceTreeResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(MallConstants.ROOT_PATH_ADMIN + "/authorization")
@Api(tags = "管理员 - 授权 API")
@Slf4j
public class AdminsAuthorizationController {

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private RoleService roleService;

    @GetMapping("/menu-resource-tree")
    @ApiOperation(value = "获得当前账号的菜单资源树", notes = "以树结构返回")
    public CommonResult<List<AdminsAuthorizationMenuTreeResponse>> menuResourceTree() {
        List<ResourceTreeNodeBO> resourceTreeNodeBOs = authorizationService.getResourceTreeByAccountId(new AuthorizationGetResourcesByAccountIdDTO()
                .setAccountId(AdminSecurityContextHolder.getAccountId()).setType(ResourceTypeEnum.MENU.getType()));
        return CommonResult.success(AdminsAuthorizationConvert.INSTANCE.convertList(resourceTreeNodeBOs));
    }

    @GetMapping("/resource-permissions")
    @ApiOperation(value = "获得当前账号的资源权限列表")
    public CommonResult<Set<String>> resourcePermissions() {
        List<ResourceBO> resources = authorizationService.getResourcesByAccountId(new AuthorizationGetResourcesByAccountIdDTO()
                .setAccountId(AdminSecurityContextHolder.getAccountId()));
        return CommonResult.success(resources.stream().map(ResourceBO::getRoute).collect(Collectors.toSet()));
    }

    @GetMapping("/role_resource_tree")
    @ApiOperation(value = "获得角色拥有的菜单权限", notes = "以树结构返回。注意，返回的资源树是完整的结构，会标记每个资源节点是否被角色所拥有")
    @ApiImplicitParam(name = "roleId", value = "角色编号", required = true, example = "1")
    @RequiresPermissions("system:authorization:assign_role_resource")
    public CommonResult<List<AdminsAuthorizationRoleResourceTreeResponse>> roleResourceTree(@RequestParam("roleId") Integer roleId) {
        // 1. 获得完整的资源树
        List<ResourceTreeNodeBO> resourceTreeNodeBOs = resourceService.getResourceTree(new ResourceGetTreeDTO());
        // 2. 获得角色拥有的子树
        Set<Integer> roleResourceIds = authorizationService.getRoleResources(new AuthorizationGetRoleResourcesDTO().setRoleId(roleId));
        // 3. 拼接，返回结果
        return CommonResult.success(AdminsAuthorizationConvert.INSTANCE.convertList(resourceTreeNodeBOs, roleResourceIds));
    }

    @PostMapping("/assign_role_resource")
    @ApiOperation(value = "分配角色资源")
    @RequiresPermissions("system:authorization:assign_role_resource")
    public CommonResult<Boolean> assignRoleResource(AdminsAuthorizationAssignRoleResourceRequest request) {
        AuthorizationAssignRoleResourceDTO authorizationAssignRoleResourceDTO = AdminsAuthorizationConvert.INSTANCE.convert(request)
                .setAdminId(AdminSecurityContextHolder.getAdminId());
        authorizationService.assignRoleResource(authorizationAssignRoleResourceDTO);
        return CommonResult.success(true);
    }

//    @GetMapping("/role_list")
//    @ApiOperation(value = "指定账号拥有的角色列表")
//    @ApiImplicitParam(name = "accountId", value = "账号编号", required = true, example = "1")
//    public CommonResult<List<AdminRoleVO>> roleList(@RequestParam("accountId") Integer accountId) {
//        // 获得所有角色列表
//        List<RoleBO> allRoleList = roleService.getRoleList();
//        // 获得管理员的角色数组
//        Set<Integer> adminRoleIdSet = CollectionUtil.convertSet(adminService.getRoleList(id), RoleBO::getId);
//        // 转换出返回结果
//        List<AdminRoleVO> result = AdminConvert.INSTANCE.convert(allRoleList);
//        // 设置每个角色是否赋予给改管理员
//        result.forEach(adminRoleVO -> adminRoleVO.setAssigned(adminRoleIdSet.contains(adminRoleVO.getId())));
//        return success(result);
//    }
//
//    @PostMapping("/assign_role")
//    @ApiOperation(value = "分配给管理员角色")
//    public CommonResult<Boolean> assignRole(AdminAssignRoleDTO adminAssignRoleDTO) {
//        return success(adminService.assignAdminRole(AdminSecurityContextHolder.getContext().getAdminId(), adminAssignRoleDTO));
//    }


}
