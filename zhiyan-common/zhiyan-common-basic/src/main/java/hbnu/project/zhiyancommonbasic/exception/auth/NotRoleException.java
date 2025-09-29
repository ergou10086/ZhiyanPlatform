package hbnu.project.zhiyancommonbasic.exception.auth;

import hbnu.project.zhiyancommonbasic.utils.StringUtils;

/**
 * 未能通过的角色认证异常
 * 
 * @author yui,asddjv
 */
public class NotRoleException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public NotRoleException(String role)
    {
        super(role);
    }

    public NotRoleException(String[] roles)
    {
        super(StringUtils.join(roles, ","));
    }
}
