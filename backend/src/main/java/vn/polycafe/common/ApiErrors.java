package vn.polycafe.common;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    ResponseEntity<?> missingHeader(Exception e) { return ResponseEntity.badRequest().body(Map.of("message","Thiếu header bắt buộc.")); }
    @ExceptionHandler(org.springframework.dao.ConcurrencyFailureException.class)
    ResponseEntity<?> concurrent(Exception e) { return ResponseEntity.status(409).body(Map.of("message","Dữ liệu đang được xử lý. Tải lại và thử lại.")); }
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    ResponseEntity<?> database(Exception e) { return ResponseEntity.status(503).body(Map.of("message","Không thể truy cập dữ liệu. Kiểm tra kết nối hoặc liên hệ quản trị.")); }
    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> business(ApiException e) { return ResponseEntity.status(e.status).body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        var errors = new java.util.LinkedHashMap<String,String>();
        e.getBindingResult().getFieldErrors().forEach(x -> errors.putIfAbsent(x.getField(), x.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng kiểm tra thông tin.", "errors", errors));
    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, org.springframework.http.converter.HttpMessageNotReadableException.class})
    ResponseEntity<?> malformed(Exception e) { return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu yêu cầu không hợp lệ.")); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflict(Exception e) { return ResponseEntity.status(409).body(Map.of("message", "Dữ liệu có xung đột. Hãy tải lại và thử lại.")); }
}
