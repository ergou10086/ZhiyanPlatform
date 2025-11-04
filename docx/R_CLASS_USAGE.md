# R 类（统一响应结果）正确用法

## 📝 问题说明

在 `WikiImportExportController.java:221` 出现编译错误，原因是 `R.fail()` 方法的**参数顺序错误**。

### ❌ 错误写法
```java
// 错误：参数顺序反了
return R.fail(result.getMessage(), result);
```

### ✅ 正确写法
```java
// 正确：先数据，后消息
return R.fail(result, result.getMessage());
```

---

## 📚 R 类所有方法签名

### 成功响应 (ok)

```java
// 1. 仅成功状态
R.ok()

// 2. 成功 + 数据
R.ok(T data)

// 3. 成功 + 数据 + 消息
R.ok(T data, String msg)
```

**示例：**
```java
return R.ok();                                    // 空数据成功
return R.ok(wikiPage);                           // 返回数据
return R.ok(wikiPage, "Wiki页面创建成功");        // 返回数据+消息
```

---

### 失败响应 (fail)

```java
// 1. 仅失败状态
R.fail()

// 2. 失败 + 消息
R.fail(String msg)

// 3. 失败 + 数据（⚠️ 注意参数顺序）
R.fail(T data)

// 4. 失败 + 数据 + 消息（⚠️ 先数据，后消息）
R.fail(T data, String msg)

// 5. 失败 + 状态码 + 消息
R.fail(int code, String msg)
```

**示例：**
```java
// 示例 1：仅返回失败状态
return R.fail();

// 示例 2：返回失败消息
return R.fail("Wiki页面不存在");

// 示例 3：返回失败数据（如验证失败的字段信息）
return R.fail(validationErrors);

// 示例 4：返回失败数据 + 消息 ✅ 先数据，后消息
WikiImportResultDTO result = importService.importFromMarkdown(file);
if (!result.getSuccess()) {
    return R.fail(result, result.getMessage());  // ✅ 正确
    // return R.fail(result.getMessage(), result);  // ❌ 错误
}

// 示例 5：返回自定义状态码
return R.fail(404, "资源未找到");
```

---

## 🎯 常见场景

### 场景 1: 创建/更新成功
```java
@PostMapping
public R<WikiPage> createWiki(@RequestBody WikiDTO dto) {
    WikiPage wiki = wikiService.create(dto);
    return R.ok(wiki, "创建成功");
}
```

### 场景 2: 查询成功
```java
@GetMapping("/{id}")
public R<WikiPage> getWiki(@PathVariable Long id) {
    WikiPage wiki = wikiService.getById(id);
    if (wiki == null) {
        return R.fail("Wiki页面不存在");
    }
    return R.ok(wiki);
}
```

### 场景 3: 删除成功
```java
@DeleteMapping("/{id}")
public R<Void> deleteWiki(@PathVariable Long id) {
    wikiService.delete(id);
    return R.ok(null, "删除成功");
}
```

### 场景 4: 导入操作（有结果对象）
```java
@PostMapping("/import")
public R<WikiImportResultDTO> importWiki(@RequestParam MultipartFile file) {
    WikiImportResultDTO result = importService.importFromMarkdown(file);
    
    if (result.getSuccess()) {
        return R.ok(result, "导入成功");  // ✅ 成功
    } else {
        return R.fail(result, result.getMessage());  // ✅ 失败但返回结果对象
    }
}
```

### 场景 5: 参数验证失败
```java
@PostMapping
public R<WikiPage> createWiki(@RequestBody WikiDTO dto) {
    if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
        return R.fail("标题不能为空");
    }
    // ...
}
```

### 场景 6: 权限验证失败
```java
@PutMapping("/{id}")
public R<WikiPage> updateWiki(@PathVariable Long id, @RequestBody WikiDTO dto) {
    if (!hasPermission(id)) {
        return R.fail(R.FORBIDDEN, "无权限编辑此Wiki");
    }
    // ...
}
```

---

## ⚠️ 注意事项

### 1. 参数顺序很重要！

```java
// ❌ 错误：参数顺序反了
R.fail(message, data)

// ✅ 正确：先数据，后消息
R.fail(data, message)
```

### 2. 泛型类型推断

有时需要明确指定泛型类型：

```java
// 如果编译器无法推断，可以显式指定
return R.<WikiImportResultDTO>fail(result, "导入失败");
```

### 3. null 安全

```java
// 如果可能返回 null，使用 ok() 的重载版本
WikiPage wiki = service.getById(id);
if (wiki == null) {
    return R.fail("不存在");
}
return R.ok(wiki);
```

---

## 🔧 故障排查

### 编译错误：找不到合适的方法

**问题：** `java: 对于fail(String, WikiImportResultDTO), 找不到合适的方法`

**原因：** 参数顺序错误

**解决：** 调换参数顺序，先数据后消息

```java
// ❌ 错误
return R.fail(result.getMessage(), result);

// ✅ 正确
return R.fail(result, result.getMessage());
```

---

## 📌 总结

| 方法 | 参数顺序 | 示例 |
|------|---------|------|
| `ok()` | 无 | `R.ok()` |
| `ok(data)` | 数据 | `R.ok(wiki)` |
| `ok(data, msg)` | **数据, 消息** | `R.ok(wiki, "成功")` |
| `fail()` | 无 | `R.fail()` |
| `fail(msg)` | 消息 | `R.fail("失败")` |
| `fail(data)` | 数据 | `R.fail(errors)` |
| `fail(data, msg)` | **数据, 消息** ⚠️ | `R.fail(result, "失败")` |
| `fail(code, msg)` | 状态码, 消息 | `R.fail(404, "未找到")` |

**记住：带数据和消息的方法，永远是先数据后消息！**

