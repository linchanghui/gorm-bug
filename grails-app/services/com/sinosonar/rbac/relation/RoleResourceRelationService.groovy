package com.sinosonar.rbac.relation

import com.sinoregal.constant.UriType
import com.sinoregal.sonar.CacheService
import com.sinosonar.rbac.accessResources.Resource
import com.sinosonar.rbac.accessResources.ResourceService
import com.sinosonar.rbac.role.SysRole
import com.sinosonar.rbac.role.SysRoleService
import grails.gorm.services.Query
import grails.gorm.services.Service
import grails.util.Holders
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean

import javax.annotation.PostConstruct

@Service(RoleResourceRelation)
abstract class RoleResourceRelationService {
    SysRoleService sysRoleService
    ResourceService resourceService

    private CacheService cacheService //这里的cacheService注入不进来，只能通过get方式获取


    public CacheService getCacheService() {
        if (cacheService == null) {
            if ( Holders.grailsApplication.mainContext.getBean("cacheService") != null) cacheService =  Holders.grailsApplication.mainContext.getBean("cacheService")
        }
        return cacheService
    }

    abstract RoleResourceRelation get(Serializable id)

    abstract List<RoleResourceRelation> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract RoleResourceRelation save(RoleResourceRelation roleResourceRelation)

    abstract List<RoleResourceRelation> findByResourceAndSysRole(Resource resource, SysRole sysRole)

    @Query("select roleResourceRelation.resource.id from $RoleResourceRelation as roleResourceRelation where roleResourceRelation.sysRole.id = ${roleId}")
    abstract List<RoleResourceRelation> findBySysRoleId(Long roleId)

    @Query("select roleResourceRelation.resource from $RoleResourceRelation as roleResourceRelation where roleResourceRelation.sysRole.id = ${roleId}")
    abstract List<Resource> findResourceBySysRoleId(Long roleId)

    boolean  commandPermissionCheck(int roleId, String command) {
        //todo 这部分改成读取缓存的
        List<Resource> resources = getCacheService().getRoleResourceCache().getUnchecked(roleId.toLong())
        List<Resource> commandResource = new ArrayList<>()
        resources.each {Resource resource ->
            if (resource.type == UriType.COMMAND)
                if (resource.uri == command)
                    commandResource.add(resource)
        }

        if (commandResource.size() == 0) {
            throw new Exception("no permission")
        }
        return false
    }

//    public void afterPropertiesSet() throws Exception {
//
//        //initialization cache
//        if (Holders.grailsApplication != null) {
//            cacheService = Holders.grailsApplication.mainContext.getBean("cacheService")
//        }
//    }

}