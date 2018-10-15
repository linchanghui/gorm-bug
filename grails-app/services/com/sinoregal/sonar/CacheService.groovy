package com.sinoregal.sonar

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sinosonar.rbac.accessResources.Resource
import com.sinosonar.rbac.relation.RoleResourceRelation
import com.sinosonar.rbac.relation.RoleResourceRelationService
import grails.events.annotation.Subscriber
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent

//import javax.annotation.PostConstruct

@Slf4j
class CacheService {
    private LoadingCache<Long, List<Resource>> roleResourceCache //只保存type为command的resource

    RoleResourceRelationService roleResourceRelationService

    RoleResourceRelation roleResourceRelationId(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof RoleResourceRelation ) {
            return ((RoleResourceRelation) event.entityObject)
        }
        null
    }

    @Subscriber
    void afterInsert(PostInsertEvent event) {
        RoleResourceRelation roleResourceRelation = roleResourceRelationId(event)
        if ( !roleResourceRelation ) {
            return
        }
        log.info 'After RoleResourceRelation save...'
        List<Resource> resources = roleResourceCache.getUnchecked(roleResourceRelation.getSysRoleId())
        if (resources == null) {
            resources = new ArrayList<>()
        }
        if (!resources.contains(roleResourceRelation))  resources.add(roleResourceRelation.resource)
        roleResourceCache.put(roleResourceRelation.getSysRoleId(), resources)

    }

    @Subscriber
    void afterDelete(PostDeleteEvent event) {
        RoleResourceRelation roleResourceRelation = roleResourceRelationId(event)
        if ( !roleResourceRelation ) {
            return
        }
        log.info 'After book delete ...'
        List<Resource> resources = roleResourceCache.getUnchecked(roleResourceRelation.getSysRoleId())
        if (resources == null) {
            resources = new ArrayList<>()
        }
        if (resources.contains(roleResourceRelation))  resources.remove(roleResourceRelation.getResource())
        roleResourceCache.put(roleResourceRelation.getSysRoleId(), resources)

    }

    public LoadingCache<Long, List<Resource>> getRoleResourceCache() {

        if (roleResourceCache == null) {
            roleResourceCache = CacheBuilder.newBuilder()
                    .recordStats()
                    .build(
                    new CacheLoader<Long, List<Resource>>() {
                        public List<Resource> load(Long key) throws Exception {

                            return roleResourceRelationService.findResourceBySysRoleId(key)
                        }
                    });
        }
        return roleResourceCache
    }

}
