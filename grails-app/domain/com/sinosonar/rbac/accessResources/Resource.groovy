package com.sinosonar.rbac.accessResources

import com.sinoregal.constant.UriType
import grails.converters.JSON

class Resource {
    static transients = ['extendProperties']
    long parentId
    String name
    String uri          //如果是接口类型，代表url。如果是页面组件，代表页面路径
    UriType type         //0/1代表页面组件或接口

//    String group
//    String label
    String path
//    String iconClassl
    String extend

    Map extendProperties

    static constraints = {
        parentId attributes: [cn: "上级节点Id"]
        path attributes:[cn: "路径"]
        uri attributes:[cn: "资源标识"]
        type attributes:[cn: "功能类型"]
        name attributes:[cn: "名称"], nullable: true
        extend attributes:[cn: "扩展字段"], nullable: true
        extendProperties attributes:[cn: "扩展字段"], bindable: true, nullable: true
        //bindable or getter method
    }

    def beforeValidate() {
        if (extendProperties)
            extend= extendProperties as JSON
    }

//    def onLoad() {
//        if (extend)
//            extendProperties=JSON.parse(extend)
//    }
}
