package com.sinosonar.rbac.accessResources

import com.sinoregal.constant.UriType
import grails.gorm.services.Query
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Transactional
@Service(Resource)
abstract class ResourceService {


    abstract Resource get(Serializable id)

    abstract List<Resource> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract Resource save(Resource Resource)

    abstract List<Resource> findByTypeAndUri(UriType type, String uri)

    @Query("select resource.id from $Resource as resource where type = ${type} and name = ${name} or path = ${path}")
    abstract List<Long> checkComponentUnique(UriType type, String name, String path)

    void initComponent(Map node, long pid) {
        //map to node pojo
        Resource componentResource = new Resource(node)
        if (!componentResource.validate()) {
            return
        }
        componentResource.parentId = pid
        componentResource.save(true)

        node.childre.each{Map it ->
            initComponent(it, componentResource.id)
        }
        return

    }

    void initCommand(String line) {
        List<String> items = line.split("\\t");
        String command = items[0].split(" ")[0];
        if (Resource.findByUri(command) != null) return
        Resource resource = new Resource(uri:command, type: UriType.COMMAND, parentId: -1 )
        if (resource.validate() ) {
            resource.save(true)
        }else {
            log.error(resource.errors.toString())
        }
    }
}
