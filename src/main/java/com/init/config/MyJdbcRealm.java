package com.init.config;

import com.init.domain.Permissions;
import com.init.domain.Roles;
import com.mybatis.PermissionsMapper;
import com.mybatis.UserMapper;
import com.mybatis.UserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MyJdbcRealm extends JdbcRealm {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PermissionsMapper permissionsMapper;

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        // Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }

        return  userMapper.obtainUsers(username)
                .stream()
                .findFirst()
                .map((user) -> new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), getName()))
               .orElseThrow(UnknownAccountException::new);
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        //null usernames are invalid
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }
        String username = (String) getAvailablePrincipal(principals);

        Set<String> roles = userRoleMapper.obtainRoles(username)
                .stream()
                .map(Roles::getRoleName)
                .collect(Collectors.toSet());

        Set<String> permissions = roles.stream()
                .flatMap(roleName -> permissionsMapper.obtainPermissions(roleName).stream())
                .map(Permissions::getPermission)
                .collect(Collectors.toSet());


        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
        info.setStringPermissions(permissions);
        return info;
    }

}
