package com.sinosonar.rbac.role

import grails.gorm.services.Service

@Service(SysRole)
abstract class SysRoleService {

    abstract SysRole get(Serializable id)

    abstract List<SysRole> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract SysRole save(SysRole sysRole)

}