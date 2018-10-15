package com.sinosonar.rbac.relation

import com.sinosonar.rbac.accessResources.Resource
import com.sinosonar.rbac.role.SysRole

/**
 * 角色资源关联表
 * @author  lch
 * @version 2014-11-17
 */
class RoleResourceRelation {
    SysRole sysRole //角色id
    Resource resource //资源id
    static constraints = {
        sysRole      attributes:[cn: "角色名"],unique: ['resource']
    }

    static m =[
            domain:[cn: "角色资源关联表"]
    ]
}
