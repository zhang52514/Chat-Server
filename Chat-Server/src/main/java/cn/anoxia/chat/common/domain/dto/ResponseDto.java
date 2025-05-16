package cn.anoxia.chat.common.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class ResponseDto {
    private String code;      // 状态码
    private String message;   // 提示信息
    private Object data;           // 返回数据

    // 构造函数
    public ResponseDto(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 快速创建成功返回
    public static ResponseDto success() {
        return new ResponseDto("200", "success", null);  // 使用状态码 200
    }

    public static ResponseDto success(Object data) {
        return new ResponseDto("200", "success", data);  // 使用状态码 200
    }

    public static ResponseDto success(String message, Object data) {
        return new ResponseDto("200", message, data);  // 使用状态码 200
    }

    // 快速创建失败返回
    public static ResponseDto failure(String code, String message) {
        return new ResponseDto(code, message, null);  // 使用其他状态码（比如 500）
    }

    public static ResponseDto failure(String code, String message, Object data) {
        return new ResponseDto(code, message, data);  // 使用其他状态码（比如 500）
    }
}

