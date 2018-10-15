package com.sinosonar.rbac.relation

import com.sinoregal.constant.UriType
import com.sinoregal.sonar.CacheService
import com.sinosonar.rbac.accessResources.Resource
import com.sinosonar.rbac.accessResources.ResourceService
import com.sinosonar.rbac.role.SysRole
import grails.converters.JSON
import grails.validation.ValidationException
import grails.web.RequestParameter

import static org.springframework.http.HttpStatus.*

class RoleResourceRelationController {

    RoleResourceRelationService roleResourceRelationService

    ResourceService resourceService


    CacheService cacheService


    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond roleResourceRelationService.list(params), model:[roleResourceRelationCount: roleResourceRelationService.count()]
    }

    def show(Long id) {

        respond roleResourceRelationService.get(id)
    }

    def create() {
        respond new RoleResourceRelation(params)
    }

    def save(RoleResourceRelation roleResourceRelation) {
        if (roleResourceRelation == null) {
            notFound()
            return
        }

        try {
            roleResourceRelationService.save(roleResourceRelation)
        } catch (ValidationException e) {
            respond roleResourceRelation.errors, [view:'create',status: BAD_REQUEST] //todo DefaultJsonRenderer.groovy:89  context.setStatus(errorsHttpStatus) will cover status parameter
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'roleResourceRelation.label', default: 'RoleResourceRelation'), roleResourceRelation.id])
                redirect roleResourceRelation
            }
            '*' { respond roleResourceRelation, [status: OK] }
        }
    }

    def edit(Long id) {
        respond roleResourceRelationService.get(id)
    }

    def update(RoleResourceRelation roleResourceRelation) {
        if (roleResourceRelation == null) {
            notFound()
            return
        }

        try {
            roleResourceRelationService.save(roleResourceRelation)
        } catch (ValidationException e) {
            respond roleResourceRelation.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'roleResourceRelation.label', default: 'RoleResourceRelation'), roleResourceRelation.id])
                redirect roleResourceRelation
            }
            '*'{ respond roleResourceRelation, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        roleResourceRelationService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'roleResourceRelation.label', default: 'RoleResourceRelation'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    def getResourceByRoleID(@RequestParameter('roleId') Long roleId) {

        List<Resource> resources = cacheService.getRoleResourceCache().getUnchecked(roleId)
        List<Resource> componentResources = new ArrayList<>()
        resources.each { Resource resource ->
            if (resource.type == UriType.COMPONENT) componentResources.add(resource)
        }
        render([data:componentResources] as JSON)
        return
    }

    def init() {
        if(RoleResourceRelation.count != 0)   RoleResourceRelation.executeUpdate("delete from RoleResourceRelation");
        List<Resource> resources = Resource.findAllByType(UriType.COMMAND)
        SysRole.all.each { SysRole role ->
            resources.each { Resource resource ->
                RoleResourceRelation roleResourceRelation = new RoleResourceRelation(
                        sysRole: role,
                        resource: resource
                )
                if (roleResourceRelation.validate() ) {
                    roleResourceRelation.save(true)
                }else {
                    log.error(roleResourceRelation.errors.toString())
                }
            }
        }

        render ([RoleResourceRelation.list()] as JSON)
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'roleResourceRelation.label', default: 'RoleResourceRelation'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }

    /**
     * 根据角色id,获取到这个角色所拥有的资源
     * @param roleId
     * @return
     */
    def getAllResourceByRoleId(@RequestParameter('roleId') Long roleId) { //用@RequestParameter来避免做params.roleId的类型判断和转换
        if (roleId == null) {
            throw new Exception("roleId is null")
        }

//        另一种实现
//        DetachedCriteria<RoleResourceRelation> relations = RoleResourceRelation.where{
//            sysRole.id == 60l
//        }
//        List<RoleResourceRelation> a = relations.findAll()
        List resourceIds = roleResourceRelationService.findBySysRoleId(roleId)
        if (resourceIds.size() == 0) {
            render([
                    data:[]
            ] as JSON)
            return
        }
        //pg的去驱动好像有bug,如果传进去的id数组是空的，会抛出异常
        List<Resource> resources = Resource.findAllByIdInList(resourceIds)
        render([
                data:resources
        ] as JSON)
    }
}
