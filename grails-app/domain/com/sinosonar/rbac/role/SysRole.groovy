package com.sinosonar.rbac.role

/**
 * 角色表
 * @author  lch
 * @version 2014-11-17
 */
class SysRole {
//    static mapping = {
//        table "role"
////        roleWeight column: "r_weight", sqlType: "int"
////        id name: 'roleWeight'
//        version false
////        id generator: 'assigned'
//
//    }
    String      roleName            //角色名
    String      roleDescription
//    Integer     roleWeight


    static constraints = {
        roleName              attributes:[cn: "角色名"],size:1..127
        roleDescription              attributes:[cn: "角色描述"],size:1..127
//        roleWeight              attributes:[cn: "角色权重"]

    }

    String toString(){
        return roleName
    }

    static m =[
            domain:[cn: "系统角色"],
            layout:[type: "standard"]
    ]
}
