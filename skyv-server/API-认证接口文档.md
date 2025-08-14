# SkyEye 认证API接口文档

## 接口概述

本文档描述了SkyEye智能监控系统的认证相关API接口，包括用户登录、登出、Token刷新等功能。

## 基础信息

- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **认证方式**: JWT Bearer Token
- **Token Header**: `Authorization: Bearer <token>`

## 1. 用户登录

### 接口信息
- **URL**: `POST /api/auth/login`
- **描述**: 用户登录接口
- **权限**: 公开接口，无需认证

### 请求参数

```json
{
  "username": "admin",           // 用户名，必填，3-50个字符
  "password": "admin123456",     // 密码，必填，6-50个字符
  "captcha": "",                 // 验证码，可选
  "captchaKey": "",             // 验证码Key，可选
  "rememberMe": false           // 记住我，可选，默认false
}
```

### 响应示例

**成功响应 (200)**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "email": "admin@skyeye.com",
      "phone": null,
      "avatar": null,
      "isAdmin": true,
      "roles": ["ADMIN"],
      "permissions": ["user:view", "user:create", "device:view", ...],
      "lastLoginTime": "2024-01-01 10:00:00",
      "lastLoginIp": "127.0.0.1"
    }
  },
  "timestamp": 1640995200000
}
```

**失败响应 (400)**:
```json
{
  "code": 608,
  "message": "登录失败",
  "timestamp": 1640995200000
}
```

### 前端对接示例

```javascript
// 登录请求
const loginApi = async (loginData) => {
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(loginData)
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      // 保存Token到localStorage或sessionStorage
      localStorage.setItem('accessToken', result.data.accessToken);
      localStorage.setItem('refreshToken', result.data.refreshToken);
      localStorage.setItem('userInfo', JSON.stringify(result.data.userInfo));
      
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('登录失败:', error);
    throw error;
  }
};
```

## 2. 用户登出

### 接口信息
- **URL**: `POST /api/auth/logout`
- **描述**: 用户登出接口
- **权限**: 需要认证

### 请求头
```
Authorization: Bearer <accessToken>
```

### 响应示例

**成功响应 (200)**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1640995200000
}
```

### 前端对接示例

```javascript
// 登出请求
const logoutApi = async () => {
  try {
    const token = localStorage.getItem('accessToken');
    
    const response = await fetch('/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      // 清除本地存储的Token和用户信息
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('userInfo');
      
      return result;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('登出失败:', error);
    throw error;
  }
};
```

## 3. 刷新Token

### 接口信息
- **URL**: `POST /api/auth/refresh`
- **描述**: 刷新访问Token
- **权限**: 公开接口，但需要有效的refreshToken

### 请求参数

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."  // 刷新Token，必填
}
```

### 响应示例

**成功响应 (200)**:
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userInfo": {
      // ... 用户信息
    }
  },
  "timestamp": 1640995200000
}
```

### 前端对接示例

```javascript
// Token刷新
const refreshTokenApi = async () => {
  try {
    const refreshToken = localStorage.getItem('refreshToken');
    
    const response = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken })
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
      // 更新Token
      localStorage.setItem('accessToken', result.data.accessToken);
      localStorage.setItem('refreshToken', result.data.refreshToken);
      
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Token刷新失败:', error);
    throw error;
  }
};
```

## 4. 检查Token有效性

### 接口信息
- **URL**: `GET /api/auth/check`
- **描述**: 检查当前Token是否有效
- **权限**: 需要认证

### 请求头
```
Authorization: Bearer <accessToken>
```

### 响应示例

**成功响应 (200)**:
```json
{
  "code": 200,
  "message": "Token有效",
  "data": true,
  "timestamp": 1640995200000
}
```

## 5. 测试接口

### 5.1 公开接口测试

- **URL**: `GET /api/auth/test/public`
- **权限**: 公开接口，无需认证

### 5.2 受保护接口测试

- **URL**: `GET /api/auth/test/protected`
- **权限**: 需要认证

### 5.3 管理员接口测试

- **URL**: `GET /api/auth/test/admin`
- **权限**: 需要管理员权限

## 前端认证流程建议

### 1. 登录流程

```javascript
// 1. 用户输入用户名密码
const handleLogin = async (formData) => {
  try {
    // 2. 调用登录API
    const loginResult = await loginApi(formData);
    
    // 3. 保存用户信息和Token
    setUserInfo(loginResult.userInfo);
    
    // 4. 跳转到主页面
    router.push('/dashboard');
    
  } catch (error) {
    // 5. 显示错误信息
    showError(error.message);
  }
};
```

### 2. HTTP请求拦截器

```javascript
// Axios请求拦截器示例
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Axios响应拦截器示例
axios.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    if (error.response?.status === 401) {
      // Token过期，尝试刷新
      try {
        await refreshTokenApi();
        // 重新发送原请求
        return axios.request(error.config);
      } catch (refreshError) {
        // 刷新失败，跳转到登录页
        localStorage.clear();
        router.push('/login');
      }
    }
    return Promise.reject(error);
  }
);
```

### 3. 路由守卫

```javascript
// Vue Router路由守卫示例
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('accessToken');
  
  if (to.path === '/login') {
    // 如果已登录，跳转到主页
    if (token) {
      next('/dashboard');
    } else {
      next();
    }
  } else {
    // 需要认证的页面
    if (token) {
      // 检查Token是否有效
      try {
        await checkTokenApi();
        next();
      } catch (error) {
        // Token无效，跳转到登录页
        localStorage.clear();
        next('/login');
      }
    } else {
      // 未登录，跳转到登录页
      next('/login');
    }
  }
});
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 401 | 未授权访问，Token无效或已过期 |
| 403 | 权限不足，用户没有访问权限 |
| 608 | 登录失败，用户名或密码错误 |
| 609 | Token已过期 |
| 610 | Token无效 |

## 默认测试账号

系统初始化时会创建以下测试账号：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123456 | 系统管理员 | 拥有所有权限 |
| user | user123456 | 普通用户 | 基本查看权限 |

## 注意事项

1. **Token存储**: 建议将accessToken存储在内存中，refreshToken存储在httpOnly cookie中以提高安全性
2. **Token过期处理**: 前端需要实现自动刷新Token的机制
3. **登录失败限制**: 系统会限制登录失败次数，连续失败5次会锁定账户30分钟
4. **CORS配置**: 后端已配置CORS，支持跨域请求
5. **HTTPS**: 生产环境建议使用HTTPS协议传输
6. **密码安全**: 建议用户定期更换密码，使用强密码策略 