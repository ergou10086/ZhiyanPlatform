package hbnu.project.zhiyancommonbasic.constants;

/**
 * Swagger相关常量
 *
 * @author ErgouTree
 */
public class SwaggerConstants {

    /**
     * API标签
     */
    public static final class Tags {
        /** 认证管理 */
        public static final String AUTH = "认证管理";
        
        /** 用户管理 */
        public static final String USER = "用户管理";
        
        /** 角色管理 */
        public static final String ROLE = "角色管理";
        
        /** 权限管理 */
        public static final String PERMISSION = "权限管理";
        
        /** 项目管理 */
        public static final String PROJECT = "项目管理";
        
        /** 团队管理 */
        public static final String TEAM = "团队管理";
        
        /** 任务管理 */
        public static final String TASK = "任务管理";
        
        /** 成果管理 */
        public static final String ARTIFACT = "成果管理";
        
        /** 知识库管理 */
        public static final String KNOWLEDGE = "知识库管理";
        
        /** 文件管理 */
        public static final String FILE = "文件管理";
        
        /** 系统管理 */
        public static final String SYSTEM = "系统管理";
    }

    /**
     * 常用操作描述
     */
    public static final class Operations {
        /** 创建 */
        public static final String CREATE = "创建";
        
        /** 更新 */
        public static final String UPDATE = "更新";
        
        /** 删除 */
        public static final String DELETE = "删除";
        
        /** 查询 */
        public static final String QUERY = "查询";
        
        /** 列表 */
        public static final String LIST = "列表";
        
        /** 详情 */
        public static final String DETAIL = "详情";
        
        /** 登录 */
        public static final String LOGIN = "登录";
        
        /** 注册 */
        public static final String REGISTER = "注册";
        
        /** 登出 */
        public static final String LOGOUT = "登出";
    }

    /**
     * 常用响应描述
     */
    public static final class Responses {
        /** 操作成功 */
        public static final String SUCCESS = "操作成功";
        
        /** 创建成功 */
        public static final String CREATE_SUCCESS = "创建成功";
        
        /** 更新成功 */
        public static final String UPDATE_SUCCESS = "更新成功";
        
        /** 删除成功 */
        public static final String DELETE_SUCCESS = "删除成功";
        
        /** 查询成功 */
        public static final String QUERY_SUCCESS = "查询成功";
        
        /** 登录成功 */
        public static final String LOGIN_SUCCESS = "登录成功";
        
        /** 注册成功 */
        public static final String REGISTER_SUCCESS = "注册成功";
        
        /** 登出成功 */
        public static final String LOGOUT_SUCCESS = "登出成功";
    }

    /**
     * 常用参数描述
     */
    public static final class Parameters {
        /** ID */
        public static final String ID = "唯一标识";
        
        /** 页码 */
        public static final String PAGE = "页码(从0开始)";
        
        /** 页大小 */
        public static final String SIZE = "每页大小";
        
        /** 排序 */
        public static final String SORT = "排序字段";
        
        /** 关键词 */
        public static final String KEYWORD = "搜索关键词";
        
        /** 开始时间 */
        public static final String START_TIME = "开始时间";
        
        /** 结束时间 */
        public static final String END_TIME = "结束时间";
    }

    /**
     * HTTP状态码描述
     */
    public static final class StatusCodes {
        /** 200 - 请求成功 */
        public static final String OK = "请求成功";
        
        /** 201 - 创建成功 */
        public static final String CREATED = "创建成功";
        
        /** 400 - 请求参数错误 */
        public static final String BAD_REQUEST = "请求参数错误";
        
        /** 401 - 未授权 */
        public static final String UNAUTHORIZED = "未授权/登录失效";
        
        /** 403 - 权限不足 */
        public static final String FORBIDDEN = "权限不足";
        
        /** 404 - 资源不存在 */
        public static final String NOT_FOUND = "资源不存在";
        
        /** 500 - 服务器内部错误 */
        public static final String INTERNAL_SERVER_ERROR = "服务器内部错误";
    }

    /**
     * 私有构造函数，防止实例化
     */
    private SwaggerConstants() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}
